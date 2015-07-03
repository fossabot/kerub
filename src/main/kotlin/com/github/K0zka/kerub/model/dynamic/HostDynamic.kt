package com.github.K0zka.kerub.model.dynamic

import com.github.K0zka.kerub.model.Entity
import java.util.UUID

/**
 * Dynamic general information about the status of a host.
 */
public data class HostDynamic(
		override val id: UUID,
		val status: HostStatus,
        val userCpu : Byte?,
        val systemCpu : Byte?,
        val idleCpu : Byte?
                        )
: Entity<UUID>
