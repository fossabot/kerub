package com.github.kerubistan.kerub.planner.steps.storage.block.duplicate

import com.github.kerubistan.kerub.model.Host
import com.github.kerubistan.kerub.model.StorageCapability
import com.github.kerubistan.kerub.model.VirtualStorageDevice
import com.github.kerubistan.kerub.model.dynamic.CompositeStorageDeviceDynamic
import com.github.kerubistan.kerub.model.dynamic.SimpleStorageDeviceDynamic
import com.github.kerubistan.kerub.model.dynamic.VirtualStorageBlockDeviceAllocation
import com.github.kerubistan.kerub.planner.OperationalState
import com.github.kerubistan.kerub.planner.costs.Cost
import com.github.kerubistan.kerub.planner.costs.NetworkCost
import com.github.kerubistan.kerub.planner.reservations.Reservation
import com.github.kerubistan.kerub.planner.reservations.UseHostReservation
import com.github.kerubistan.kerub.planner.reservations.VirtualStorageReservation
import com.github.kerubistan.kerub.planner.steps.AbstractOperationalStep
import com.github.kerubistan.kerub.planner.steps.InvertibleStep
import com.github.kerubistan.kerub.planner.steps.base.AbstractUnAllocate
import io.github.kerubistan.kroki.collections.update
import java.math.BigInteger

abstract class AbstractBlockDuplicate<T : VirtualStorageBlockDeviceAllocation> : AbstractOperationalStep,
		InvertibleStep {

	abstract val virtualStorageDevice: VirtualStorageDevice
	abstract val source: VirtualStorageBlockDeviceAllocation
	abstract val sourceHost: Host
	abstract val target: T
	abstract val targetHost: Host
	abstract val targetCapability : StorageCapability

	override fun isInverseOf(other: AbstractOperationalStep): Boolean =
			(other is AbstractUnAllocate<*>
					&& other.vstorage == virtualStorageDevice
					&& other.allocation == target
					&& other.host == targetHost)

	override fun take(state: OperationalState): OperationalState =
			state.copy(
					vStorage = state.vStorage.update(virtualStorageDevice.id) {
						it.copy(
								dynamic = requireNotNull(it.dynamic) {
									"can't duplicate storage ${virtualStorageDevice.id}, it is not yet allocated"
								}.let {
									it.copy(
											allocations = it.allocations + target
									)
								}
						)
					},
					//update the host storage allocation
					hosts = state.hosts.update(targetHost.id) {
						it.copy(
								dynamic = it.dynamic!!.copy(
										storageStatus = it.dynamic.storageStatus.map {
											storageStatus ->
											if(storageStatus.id == targetCapability.id) {
												when(storageStatus) {
													is SimpleStorageDeviceDynamic ->
														storageStatus.copy(
																freeCapacity = (storageStatus.freeCapacity - target.actualSize)
																		.coerceAtLeast(BigInteger.ZERO)
														)
													is CompositeStorageDeviceDynamic ->
														storageStatus.copy(
																reportedFreeCapacity = (storageStatus.freeCapacity - target.actualSize)
																		.coerceAtLeast(BigInteger.ZERO)
														)
													else ->
														TODO("Unhandled type: ${storageStatus.javaClass.name}")
												}
											} else {
												storageStatus
											}
										}
								)
						)
					}
			)

	override fun reservations(): List<Reservation<*>> = listOf(
			UseHostReservation(targetHost), UseHostReservation(sourceHost),
			VirtualStorageReservation(virtualStorageDevice)
	)

	override fun getCost(): List<Cost> = listOf(
			NetworkCost(hosts = listOf(sourceHost, targetHost), bytes = virtualStorageDevice.size.toLong())
	)
}