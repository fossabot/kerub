package com.github.kerubistan.kerub.planner.steps.storage.share.iscsi

import com.github.kerubistan.kerub.model.config.HostConfiguration
import com.github.kerubistan.kerub.model.services.IscsiService
import com.github.kerubistan.kerub.planner.OperationalState
import io.github.kerubistan.kroki.collections.update

interface AbstractIscsiShare : AbstractIscsiOperation {

	override fun take(state: OperationalState): OperationalState
			= state.copy(
			hosts = state.hosts.update(host.id) { hostData ->
				val hostConfig = hostData.config
				hostData.copy(
						config = hostConfig?.copy(services = hostConfig.services + IscsiService(vstorage.id))
								?: HostConfiguration(id = host.id, services = listOf(IscsiService(vstorage.id)))
				)
			}
	)

}