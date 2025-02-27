package com.github.kerubistan.kerub.host.distros

import com.github.kerubistan.kerub.data.dynamic.HostDynamicDao
import com.github.kerubistan.kerub.data.dynamic.VirtualStorageDeviceDynamicDao
import com.github.kerubistan.kerub.host.FireWall
import com.github.kerubistan.kerub.host.PackageManager
import com.github.kerubistan.kerub.host.ServiceManager
import com.github.kerubistan.kerub.host.executeOrDie
import com.github.kerubistan.kerub.model.Host
import com.github.kerubistan.kerub.model.HostCapabilities
import com.github.kerubistan.kerub.model.OperatingSystem
import com.github.kerubistan.kerub.model.SoftwarePackage
import com.github.kerubistan.kerub.model.StorageCapability
import com.github.kerubistan.kerub.model.Version
import com.github.kerubistan.kerub.model.controller.config.ControllerConfig
import com.github.kerubistan.kerub.model.hardware.BlockDevice
import com.github.kerubistan.kerub.model.lom.PowerManagementInfo
import com.github.kerubistan.kerub.utils.junix.common.OsCommand
import com.github.kerubistan.kerub.utils.junix.uname.UName
import org.apache.sshd.client.session.ClientSession
import java.math.BigInteger

class OpenIndiana : Distribution {
	override fun listBlockDevices(session: ClientSession): List<BlockDevice> =
			// TODO: find a way to extract this information from opensolaris
			listOf()

	override val operatingSystem: OperatingSystem = OperatingSystem.Unix

	override fun getVersion(session: ClientSession): Version
			= Version.fromVersionString(UName.kernelVersion(session))

	override fun name(): String = "openindiana"

	override fun handlesVersion(version: Version): Boolean = version.major == "5"

	override fun detect(session: ClientSession): Boolean = session.executeOrDie("uname -n") == name()

	override fun getPackageManager(session: ClientSession): PackageManager
			= TODO("https://github.com/kerubistan/kerub/issues/180")

	override fun installMonitorPackages(session: ClientSession, host: Host) {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun startMonitorProcesses(
			session: ClientSession,
			host: Host,
			hostDynDao: HostDynamicDao,
			vStorageDeviceDynamicDao: VirtualStorageDeviceDynamicDao,
			controllerConfig: ControllerConfig
	) {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun getRequiredPackages(osCommand: OsCommand, capabilities: HostCapabilities?): List<String> {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun detectStorageCapabilities(
			session: ClientSession,
			osVersion: SoftwarePackage,
			packages: List<SoftwarePackage>
	): List<StorageCapability> {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun detectPowerManagement(session: ClientSession): List<PowerManagementInfo> {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun detectHostCpuType(session: ClientSession): String = session.executeOrDie("uname -p")

	override fun getTotalMemory(session: ClientSession): BigInteger {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun getFireWall(session: ClientSession): FireWall {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun getServiceManager(session: ClientSession): ServiceManager {
		TODO("https://github.com/kerubistan/kerub/issues/180")
	}

	override fun getHostOs(): OperatingSystem = OperatingSystem.Unix
}