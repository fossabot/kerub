package com.github.K0zka.kerub.data.ispn

import com.github.K0zka.kerub.data.dynamic.ControllerDynamicDao
import com.github.K0zka.kerub.model.dynamic.ControllerDynamic
import org.infinispan.Cache

public class ControllerDynamicDaoImpl(val cache: Cache<String, ControllerDynamic>) : ControllerDynamicDao {
	override fun listAll(): List<ControllerDynamic> {
		return cache.map { it.value }
	}

	override fun add(entity: ControllerDynamic): String {
		cache.put(entity.id, entity)
		return entity.controllerId
	}

	override fun get(id: String): ControllerDynamic? = cache[id]
}