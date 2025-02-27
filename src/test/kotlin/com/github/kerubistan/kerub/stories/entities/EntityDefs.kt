package com.github.kerubistan.kerub.stories.entities

import com.github.kerubistan.kerub.createClient
import com.github.kerubistan.kerub.login
import com.github.kerubistan.kerub.model.Account
import com.github.kerubistan.kerub.model.Entity
import com.github.kerubistan.kerub.model.Pool
import com.github.kerubistan.kerub.model.Range
import com.github.kerubistan.kerub.model.VirtualMachine
import com.github.kerubistan.kerub.model.VirtualNetwork
import com.github.kerubistan.kerub.model.VirtualStorageDevice
import com.github.kerubistan.kerub.runRestAction
import com.github.kerubistan.kerub.services.AccountService
import com.github.kerubistan.kerub.services.PoolService
import com.github.kerubistan.kerub.services.RestCrud
import com.github.kerubistan.kerub.services.VirtualMachineService
import com.github.kerubistan.kerub.services.VirtualNetworkService
import com.github.kerubistan.kerub.services.VirtualStorageDeviceService
import com.github.kerubistan.kerub.testDisk
import com.github.kerubistan.kerub.testVm
import com.github.kerubistan.kerub.utils.getLogger
import com.github.kerubistan.kerub.utils.toSize
import cucumber.api.DataTable
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import java.util.UUID
import kotlin.reflect.KClass

class EntityDefs {

	val client = createClient()

	private var entities = mapOf<String, Entity<UUID>>()

	companion object {
		private val logger = getLogger(EntityDefs::class)
		private val insanceTl = ThreadLocal<EntityDefs>()

		val instance: EntityDefs
			get() = insanceTl.get()
	}

	fun entityDropped(entity : Entity<*>) {
		entities = entities.filterValues { it.id != entity.id }
	}

	private val entityTypes = mapOf<KClass<out Any>, KClass<out RestCrud<*>>>(
			VirtualMachine::class to VirtualMachineService::class,
			VirtualNetwork::class to VirtualNetworkService::class,
			VirtualStorageDevice::class to VirtualStorageDeviceService::class,
			Pool::class to PoolService::class
	)

	@Given("Account:")
	fun createAccount(details: DataTable) {
		var account = Account(
				id = UUID.randomUUID(),
				name = "TEST"
		)
		details.forEachPair { name, value ->
			account = when (name) {
				Account::name.name -> account.copy(name = value)
				Account::requireProjects.name -> account.copy(requireProjects = value.toBoolean())
				else -> TODO("Not mapped property for account: $name")
			}
		}
		client.runRestAction(AccountService::class) {
			entities += account.name to it.add(account)
		}
	}

	@Given("Virtual Disk:")
	fun createVirtualDisk(details: DataTable) {
		var disk = testDisk.copy(id = UUID.randomUUID())
		details.forEachPair { name, value ->
			disk = when (name) {
				VirtualStorageDevice::name.name -> disk.copy(name = value)
				VirtualStorageDevice::size.name -> disk.copy(size = value.toSize())
				VirtualStorageDevice::readOnly.name -> disk.copy(readOnly = value.toBoolean())
				else -> TODO("Not mapped property for virtual disk: $name")
			}
		}
		client.runRestAction(VirtualStorageDeviceService::class) {
			entities += disk.name to it.add(disk)
		}
	}

	@Given("Virtual Pool:")
	fun createVirtualPool(details: DataTable) {
		var pool = Pool(name = "test pool", templateId = UUID.randomUUID())
		details.forEachPair { name, value ->
			when (name) {
				Pool::name.name -> pool = pool.copy(name = value)
				else -> TODO("Not mapped property for virtual disk: $name")
			}
		}
		client.runRestAction(PoolService::class) {
			entities += pool.name to it.add(pool)
		}
	}

	@Given("Virtual Network:")
	fun createVirtualNetwork(details: DataTable) {
		var network = VirtualNetwork(
				id = UUID.randomUUID(),
				name = "test-network"
		)
		details.forEachPair { name, value ->
			when (name) {
				VirtualNetwork::name.name -> network = network.copy(name = value)
				else -> TODO("Not mapped property for virtual network: $name")
			}
		}
		client.runRestAction(VirtualNetworkService::class) {
			entities += network.name to it.add(network)
		}
	}

	@Given("VM:")
	fun createVm(details: DataTable) {
		var vm = testVm.copy(id = UUID.randomUUID())
		details.forEachPair {
			propName, propValueStr ->
			when (propName) {
				VirtualMachine::id.name -> vm = vm.copy(id = UUID.fromString(propValueStr))
				VirtualMachine::name.name -> vm = vm.copy(name = propValueStr)
				VirtualMachine::memory.name -> {
					val memSizeRange = if (propValueStr.contains('-')) {
						Range(
								propValueStr.substringBefore('-').toSize(),
								propValueStr.substringAfter('-').toSize()
						)
					} else {
						Range(propValueStr.toSize(), propValueStr.toSize())
					}
					vm = vm.copy(memory = memSizeRange)
				}
				VirtualMachine::nrOfCpus.name -> vm = vm.copy(nrOfCpus = propValueStr.toInt())
				else -> TODO("Not mapped: $propName")
			}
		}
		client.runRestAction(VirtualMachineService::class) {
			entities += vm.name to it.add(vm)
		}
	}

	fun getEntity(name: String) =
			requireNotNull(entities[name]) { "Entity $name not found in registry of test entities" }

	@Before
	fun setup() {
		insanceTl.set(this)
		client.login("admin", "password")
	}

	@After
	fun cleanup() {
		insanceTl.remove()
		entities.forEach {
			kv ->
			logger.debug("cleaning up {} (ID: {})", kv.key, kv.value.id)
			val serviceClass = requireNotNull(entityTypes[kv.value.javaClass.kotlin]) {
				"Hey, no service for ${kv.value.javaClass.kotlin}"
			}
			client.runRestAction(serviceClass) {
				it.delete(kv.value.id)
			}
			logger.info("deleted {} (ID: {})", kv.key, kv.value.id)
		}
	}

}