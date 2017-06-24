package com.github.K0zka.kerub.planner.steps.vstorage

import com.github.K0zka.kerub.model.VirtualStorageDevice
import com.github.K0zka.kerub.model.dynamic.HostStatus
import com.github.K0zka.kerub.model.expectations.StorageAvailabilityExpectation
import com.github.K0zka.kerub.model.expectations.VirtualMachineAvailabilityExpectation
import com.github.K0zka.kerub.model.io.VirtualDiskFormat
import com.github.K0zka.kerub.planner.OperationalState
import com.github.K0zka.kerub.planner.steps.AbstractOperationalStep
import com.github.K0zka.kerub.planner.steps.AbstractOperationalStepFactory

abstract class AbstractCreateVirtualStorageFactory<S : AbstractOperationalStep> : AbstractOperationalStepFactory<S>() {

	companion object {
		fun listRunningHosts(state: OperationalState) =
				state.hosts.values.filter { it.dynamic?.status == HostStatus.Up }

		fun listStorageNotAllocated(
				state: OperationalState,
				types: List<VirtualDiskFormat> = VirtualDiskFormat.values().toList()): List<VirtualStorageDevice> {
			val vmsThatMustRun = state.vms.values.filter {
				vm ->
				vm.stat.expectations.any {
					expectation ->
					expectation is VirtualMachineAvailabilityExpectation
							&& expectation.up
				}
			}
			val storageNotAllocated = state.vStorage.values.filter { it.dynamic == null }
					.filter {
						storage ->
						storage.stat.expectations.any {
							it is StorageAvailabilityExpectation
									&& types.contains(it.format)
						}
								||
								vmsThatMustRun.any {
									vm ->
									vm.stat.virtualStorageLinks.any {
										link ->
										link.virtualStorageId == storage.stat.id
									}
								}
					}
			return storageNotAllocated.map { it.stat }
		}

	}

}