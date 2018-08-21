package com.github.kerubistan.kerub.host

import com.github.kerubistan.kerub.data.ControllerConfigDao
import com.github.kerubistan.kerub.host.distros.Centos6
import com.github.kerubistan.kerub.host.distros.Centos7
import com.github.kerubistan.kerub.host.distros.Cygwin
import com.github.kerubistan.kerub.host.distros.Debian
import com.github.kerubistan.kerub.host.distros.Distribution
import com.github.kerubistan.kerub.host.distros.Fedora
import com.github.kerubistan.kerub.host.distros.Fedora23Plus
import com.github.kerubistan.kerub.host.distros.FreeBSD
import com.github.kerubistan.kerub.host.distros.Gentoo
import com.github.kerubistan.kerub.host.distros.OpenIndiana
import com.github.kerubistan.kerub.host.distros.OpenSuse
import com.github.kerubistan.kerub.host.distros.Raspbian
import com.github.kerubistan.kerub.host.distros.Ubuntu
import com.github.kerubistan.kerub.host.distros.XenServer7
import com.github.kerubistan.kerub.model.HostCapabilities
import com.github.kerubistan.kerub.model.SoftwarePackage
import com.github.kerubistan.kerub.model.StorageCapability
import com.github.kerubistan.kerub.model.controller.config.ControllerConfig
import com.github.kerubistan.kerub.model.hardware.ChassisInformation
import com.github.kerubistan.kerub.model.hardware.MemoryInformation
import com.github.kerubistan.kerub.model.hardware.ProcessorInformation
import com.github.kerubistan.kerub.model.hardware.SystemInformation
import com.github.kerubistan.kerub.services.exc.UnknownHostOperatingSystemException
import com.github.kerubistan.kerub.utils.getLogger
import com.github.kerubistan.kerub.utils.junix.dmi.DmiDecoder
import com.github.kerubistan.kerub.utils.junix.lspci.LsPci
import com.github.kerubistan.kerub.utils.junix.virt.virsh.Virsh
import com.github.kerubistan.kerub.utils.silent
import org.apache.sshd.client.session.ClientSession
import kotlin.reflect.KClass

/**
 * Helper class to detect host capabilities through an established SSH session.
 */
class HostCapabilitiesDiscovererImpl(private val controllerConfigDao: ControllerConfigDao) : HostCapabilitiesDiscoverer {

	companion object {
		private val logger = getLogger(HostCapabilitiesDiscovererImpl::class)
		internal val distributions = listOf(
				//RPM-based distros
				Fedora(),
				Fedora23Plus(),
				Gentoo(),
				Centos6(),
				Centos7(),
				OpenSuse(),
				XenServer7(),
				// Debian-family
				Debian(),
				Ubuntu(),
				Raspbian(),
				// The BSD's
				FreeBSD(),
				//'UNIX'es
				OpenIndiana(),
				//Windows
				Cygwin()
		)

		internal fun detectAndBenchmark(distro: Distribution,
										session: ClientSession,
										distribution: SoftwarePackage,
										packages: List<SoftwarePackage>,
										controllerConfig: ControllerConfig): List<StorageCapability> =
				distro.detectStorageCapabilities(session, distribution, packages).let {
					if (controllerConfig.storageTechnologies.storagebenchmarkingEnbled) {
						logger.info("benchmarking enabled storage")
						it.map {
							distro.storageBenchmark(session, it, distribution, packages, controllerConfig.storageTechnologies)
						}
					} else
						it
				}

	}

	private fun <T : Any> valuesOfType(list: Collection<*>, clazz: KClass<T>): List<T> {
		return list.filter { it?.javaClass?.kotlin == clazz }.map { clazz.java.cast(it) }
	}

	override
	fun discoverHost(session: ClientSession, dedicated: Boolean): HostCapabilities {

		val distro = detectDistro(session)
		val cpuArchitecture = distro.detectHostCpuType(session)
		val packages = silent { distro.getPackageManager(session).list() } ?: listOf()
		val dmiDecodeInstalled = installDmi(dedicated, distro, packages, session)
		val systemInfo = if (dmiDecodeInstalled) DmiDecoder.parse(runDmiDecode(session)) else mapOf()

		val osDetectedFlags = distro.detectHostCpuFlags(session)
		val hardwareInfo = systemInfo.values
		val hostOs = distro.getHostOs()
		val distribution = SoftwarePackage(distro.name(), distro.getVersion(session))

		val hypervisorCapabilities =
				if (Virsh.available(distribution, packages))
					silent { listOf(Virsh.capabilities(session)) } ?: listOf()
				else
					listOf()

		val storageCapabilities = detectAndBenchmark(distro, session, distribution, packages, controllerConfigDao.get())

		return HostCapabilities(
				os = hostOs,
				cpuArchitecture = cpuArchitecture,
				distribution = distribution,
				installedSoftware = packages,
				totalMemory = distro.getTotalMemory(session),
				memoryDevices = valuesOfType(hardwareInfo, MemoryInformation::class),
				system = valuesOfType(hardwareInfo, SystemInformation::class).firstOrNull(),
				cpus = valuesOfType(hardwareInfo, ProcessorInformation::class).map {
					it.copy(
							flags = (it.flags + osDetectedFlags).toSet().toList()
					)
				},
				chassis = valuesOfType(hardwareInfo, ChassisInformation::class).firstOrNull(),
				devices = LsPci.execute(session),
				powerManagment = distro.detectPowerManagement(session),
				storageCapabilities = storageCapabilities,
				hypervisorCapabilities = hypervisorCapabilities
		)
	}

	private fun installDmi(dedicated: Boolean, distro: Distribution?, packages: List<SoftwarePackage>, session: ClientSession): Boolean {
		val dmiDecodeInstalled = isDmiDecodeInstalled(packages)
		if (!dmiDecodeInstalled && dedicated && distro != null) {
			distro.getPackageManager(session).install("dmidecode")
			return true
		}
		return dmiDecodeInstalled
	}

	private fun runDmiDecode(session: ClientSession): String =
			session.execute("dmidecode")

	private fun isDmiDecodeInstalled(packages: List<SoftwarePackage>): Boolean {
		return packages.any { "dmidecode" == it.name }
	}

	override fun detectDistro(session: ClientSession): Distribution {
		for (distro in distributions) {
			logger.debug("Checking host with ${distro.name()} distro helper")
			if (distro.detect(session)) {
				return distro
			}
		}
		throw UnknownHostOperatingSystemException("None of the distributions matched: "
				+ distributions.joinToString(",") { "${it.operatingSystem}/${it.name()}" })
	}


}