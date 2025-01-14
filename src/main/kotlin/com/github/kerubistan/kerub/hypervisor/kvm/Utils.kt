package com.github.kerubistan.kerub.hypervisor.kvm

import com.github.kerubistan.kerub.model.Host
import com.github.kerubistan.kerub.model.VirtualMachine
import com.github.kerubistan.kerub.model.VirtualStorageLinkInfo
import com.github.kerubistan.kerub.model.dynamic.VirtualStorageFsAllocation
import com.github.kerubistan.kerub.model.dynamic.VirtualStorageLvmAllocation
import com.github.kerubistan.kerub.model.services.IscsiService
import com.github.kerubistan.kerub.model.services.NfsService
import com.github.kerubistan.kerub.utils.storage.iscsiStorageId
import com.github.kerubistan.kerub.utils.storage.iscsiDefaultUser as iscsiUser

fun storagesToXml(disks: List<VirtualStorageLinkInfo>, targetHost: Host): String {
	return buildString(disks.size * 256) {
		var targetDev = 'a'
		for (link in disks) {
			append(storageToXml(link, targetHost, targetDev))
			targetDev++
		}
	}
}

val allocationTypeToDiskType = mapOf(
		VirtualStorageFsAllocation::class to "file",
		VirtualStorageLvmAllocation::class to "block"
)

private fun storageToXml(
		linkInfo: VirtualStorageLinkInfo, targetHost: Host, targetDev: Char): String = """
			<disk type='${kvmDeviceType(linkInfo, targetHost)}' device='${linkInfo.link.device.name.toLowerCase()}'>
				<driver name='qemu' type='${allocationType(linkInfo)}' cache='none'/>
				${if (linkInfo.device.stat.readOnly || linkInfo.link.readOnly) "<readonly/>" else ""}
				${allocationToXml(linkInfo, targetHost)}
				<target dev='sd$targetDev' bus='${linkInfo.link.bus}'/>
			</disk>
"""

private fun kvmDeviceType(linkInfo: VirtualStorageLinkInfo, targetHost: Host): String =
		if (isRemoteHost(linkInfo, targetHost)) {
			when(linkInfo.hostServiceUsed) {
				is NfsService -> "file"
				is IscsiService -> "network"
				else -> TODO("not handled service type: $linkInfo")
			}
		} else {
			allocationTypeToDiskType[linkInfo.allocation.javaClass.kotlin] ?: TODO()
		}

fun allocationType(deviceDyn: VirtualStorageLinkInfo): String = deviceDyn.allocation.let {
	when (it) {
		is VirtualStorageLvmAllocation -> "raw"
		is VirtualStorageFsAllocation -> it.type.name.toLowerCase()
		else -> TODO("")
	}
}

fun allocationToXml(linkInfo: VirtualStorageLinkInfo, targetHost: Host): String =
		if (isRemoteHost(linkInfo, targetHost)) {
			when(linkInfo.hostServiceUsed) {
				is NfsService ->
					"""
						<!-- nfs -->
						<source file='/mnt/${linkInfo.allocation.hostId}/${linkInfo.allocation.getPath(linkInfo.device.stat.id)}'/>
					""".trimIndent()
				is IscsiService -> {
					val auth = if(linkInfo.hostServiceUsed.password != null) {
						"""
							<auth username="$iscsiUser">
								<secret type='iscsi'  uuid='${linkInfo.device.stat.id}'/>
							</auth>
						""".trimIndent()
					} else "<!-- unauthenticated -->"

					"""
					<!-- iscsi -->
					<source protocol='iscsi' name='${iscsiStorageId(linkInfo.device.stat.id)}/1'>
						<host name='${linkInfo.storageHost.stat.address}' port='3260' />
					</source>
					$auth
					""".trimIndent()
			}
				else -> TODO("not handled service type: $linkInfo")
			}
		} else {
			val allocation = linkInfo.allocation
			when (allocation) {
				is VirtualStorageFsAllocation ->
					"""
						<!-- local ${allocation.type} file allocation -->
						<source file='${allocation.fileName}'/>
					""".trimIndent()
				is VirtualStorageLvmAllocation ->
					"""
						<!-- local lvm allocation -->
						<source dev='${allocation.path}'/>
					""".trimMargin()
				else -> TODO()
			}
		}

fun isRemoteHost(linkInfo: VirtualStorageLinkInfo, targetHost: Host) = linkInfo.allocation.hostId != targetHost.id

fun vmDefinitiontoXml(
		vm: VirtualMachine, disks: List<VirtualStorageLinkInfo>, password: String, targetHost: Host
): String =
		"""
	<domain type='kvm'>
		<name>${vm.id}</name>
		<uuid>${vm.id}</uuid>
		<memory unit='B'>${vm.memory.min}</memory>
		<memtune>
			<hard_limit unit='B'>${vm.memory.min}</hard_limit>
		</memtune>
		<memoryBacking>
			<allocation mode="ondemand"/>
		</memoryBacking>
		<vcpu>${vm.nrOfCpus}</vcpu>
		<os>
			<type arch='x86_64'>hvm</type>
			<boot dev='hd'/>
			<boot dev='cdrom'/>
		</os>
		<features>
			<acpi/>
			<apic/>
			<pae/>
			<hap/>
		</features>
		<devices>
			<input type='keyboard' bus='ps2'/>
			<graphics type='spice' autoport='yes' listen='0.0.0.0' passwd='$password'>
				<listen type='address' address='0.0.0.0'/>
				<image compression='off'/>
			</graphics>
			<video>
				<model type='qxl' ram='65536' vram='65536' vgamem='16384' heads='1'/>
				<address type='pci' domain='0x0000' bus='0x00' slot='0x02' function='0x0'/>
			</video>
			${storagesToXml(disks, targetHost)}
		</devices>
	</domain>
	"""
