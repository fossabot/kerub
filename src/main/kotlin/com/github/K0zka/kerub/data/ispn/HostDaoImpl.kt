package com.github.K0zka.kerub.data.ispn

import com.github.K0zka.kerub.data.EventListener
import com.github.K0zka.kerub.data.HostDao
import com.github.K0zka.kerub.model.Host
import org.infinispan.Cache
import java.util.UUID

public class HostDaoImpl(cache: Cache<UUID, Host>, eventListener: EventListener) : ListableIspnDaoBase<Host, UUID>(cache, eventListener), HostDao {
	override fun getEntityClass(): Class<Host> {
		return Host::class.java
	}
}