package com.github.kerubistan.kerub.planner.steps.storage.lvm.pool.create

import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.k0zka.finder4j.backtrack.Step
import com.github.kerubistan.kerub.model.Host
import com.github.kerubistan.kerub.model.LvmStorageCapability
import com.github.kerubistan.kerub.model.config.HostConfiguration
import com.github.kerubistan.kerub.model.config.LvmPoolConfiguration
import com.github.kerubistan.kerub.planner.OperationalState
import com.github.kerubistan.kerub.planner.Plan
import com.github.kerubistan.kerub.planner.reservations.HostStorageReservation
import com.github.kerubistan.kerub.planner.reservations.Reservation
import com.github.kerubistan.kerub.planner.reservations.UseHostReservation
import com.github.kerubistan.kerub.planner.steps.AbstractOperationalStep
import com.github.kerubistan.kerub.planner.steps.SimilarStep
import com.github.kerubistan.kerub.planner.steps.storage.lvm.base.updateHostDynLvmWithAllocation
import com.github.kerubistan.kerub.planner.steps.storage.lvm.create.CreateThinLv
import com.github.kerubistan.kerub.planner.steps.storage.lvm.pool.common.volumeGroupId
import com.github.kerubistan.kerub.planner.steps.storage.migrate.dead.block.MigrateBlockAllocation
import io.github.kerubistan.kroki.collections.update
import java.math.BigInteger

@JsonTypeName("create-lvm-pool")
data class CreateLvmPool(
		val host: Host,
		val vgName: String,
		val name: String,
		val size: BigInteger) : AbstractOperationalStep, SimilarStep {

	init {
		val capability = requireNotNull(host.capabilities?.storageCapabilities)
				.filterIsInstance<LvmStorageCapability>()
				.first {it.volumeGroupName == vgName }
		check(capability.size > size) {
			"lvm capability ${host.address}/${capability.volumeGroupName} size (${capability.size})" +
					" is less than the requested pool size $size"
		}
	}

	override fun isLikeStep(other: Step<Plan>)
			= other is CreateLvmPool && other.host == host && other.vgName == vgName

	override val useBefore get() = listOf(CreateThinLv::class, MigrateBlockAllocation::class)

	override fun reservations(): List<Reservation<*>> = listOf(
			UseHostReservation(host),
			HostStorageReservation(
					host,
					size,
					volumeGroupId(host.capabilities!!.storageCapabilities, vgName)
			)
	)

	override fun take(state: OperationalState): OperationalState = state.copy(
			hosts = state.hosts.update(host.id) {
				it.copy(
						config = (it.config ?: HostConfiguration()).let { config ->
							config.copy(
									storageConfiguration = config.storageConfiguration +
											LvmPoolConfiguration(
													vgName = this.vgName,
													size = this.size,
													poolName = this.name
											)
							)
						},
						dynamic = updateHostDynLvmWithAllocation(state, host, vgName, size)
				)
			}
	)
}