package com.github.kerubistan.kerub.utils.junix.bridgectl

import com.github.kerubistan.kerub.host.executeOrDie
import com.github.kerubistan.kerub.utils.junix.common.OsCommand
import com.github.kerubistan.kerub.utils.skip
import io.github.kerubistan.kroki.collections.replace
import org.apache.sshd.client.session.ClientSession

object BridgeCtl : OsCommand {

	private val fieldSeparator = "\\s+".toRegex()

	fun list(session: ClientSession): List<Bridge> {
		var bridges = listOf<Bridge>()
		session.executeOrDie("brctl show").lines().skip().forEach {
			val fields = it.trim().split(fieldSeparator)
			when (fields.size) {
				1 -> {
					val last = bridges.last()
					val ifname = fields[0]
					if (ifname.isNotBlank()) {
						bridges = bridges.replace({ it.id == last.id }, {
							it.copy(
									ifaces = it.ifaces + fields[0]
							)
						})
					}
				}
				3 -> {
					bridges += Bridge(
							name = fields[0],
							id = fields[1],
							stpEnabled = "yes" == fields[2],
							ifaces = listOf()
					)
				}
				4 -> {
					bridges += Bridge(
							name = fields[0],
							id = fields[1],
							stpEnabled = "yes" == fields[2],
							ifaces = if (fields[3].isBlank()) listOf() else listOf(fields[3])
					)
				}
			}
		}
		return bridges
	}

}