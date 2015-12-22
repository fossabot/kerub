package com.github.K0zka.kerub.planner.steps.vm.base

import com.github.K0zka.kerub.model.dynamic.VirtualMachineDynamic
import com.github.K0zka.kerub.planner.OperationalState
import com.github.K0zka.kerub.planner.steps.AbstractOperationalStep
import com.github.K0zka.kerub.planner.steps.AbstractOperationalStepFactory
import java.util.UUID

abstract class AbstractForEachVmStepFactory<T : AbstractOperationalStep> : AbstractOperationalStepFactory<T>() {

	abstract fun filter(vmDyn: VirtualMachineDynamic): Boolean

	abstract fun create(vmDyn: VirtualMachineDynamic, state: OperationalState): T

	protected fun getVm(id: UUID, state: OperationalState) =
			requireNotNull(state.vms[id], { "VM not found with id $id" })

	protected fun getHost(id: UUID, state: OperationalState) =
			requireNotNull(state.hosts[id], { "Host not found with id $id" })

	override final fun produce(state: OperationalState): List<T> {
		return state.vmDyns.values
				.filter { filter(it) }
				.map {
					vmDyn ->
					create(vmDyn, state)
				}
	}
}