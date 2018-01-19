package com.github.kerubistan.kerub.planner.issues.problems.hosts

import com.github.kerubistan.kerub.model.VirtualMachineStatus
import com.github.kerubistan.kerub.model.services.HostService
import com.github.kerubistan.kerub.model.services.IscsiService
import com.github.kerubistan.kerub.planner.OperationalState
import com.github.kerubistan.kerub.planner.Plan
import com.github.kerubistan.kerub.planner.issues.problems.ProblemDetector
import com.github.kerubistan.kerub.utils.join

object UnusedServiceDetector : ProblemDetector<UnusedService> {
	override fun detect(plan: Plan): Collection<UnusedService> =
			plan.state.hosts.values.map { hostColl ->
				hostColl.config?.services?.filterNot { isUsed(it, plan.state) }
						?.map { UnusedService(host = hostColl.stat, service = it) }
						?: listOf()
			}.join()

	private fun isUsed(hostService: HostService, state: OperationalState): Boolean =
			when (hostService) {
				is IscsiService -> {
					//any vm's that is running and is using this virtual storage
					state.vms.values.any {
						it.dynamic?.status == VirtualMachineStatus.Up
								&& it.stat.virtualStorageLinks.any { it.virtualStorageId == hostService.vstorageId }
					}
				}
				else -> TODO()
			}
}