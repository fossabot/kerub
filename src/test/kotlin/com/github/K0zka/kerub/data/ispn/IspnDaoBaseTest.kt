package com.github.K0zka.kerub.data.ispn

import com.github.K0zka.kerub.data.EventListener
import com.github.K0zka.kerub.model.Entity
import org.infinispan.Cache
import org.infinispan.manager.DefaultCacheManager
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class IspnDaoBaseTest {

	class TestEntity : Entity<String>{
		override var id: String = "TEST"
	}

	class TestDao (cache :Cache<String, TestEntity>, eventListener : EventListener)
		: ListableIspnDaoBase<TestEntity, String>(cache, eventListener) {
		override fun getEntityClass(): Class<TestEntity> {
			return TestEntity::class.java
		}


	}

	var cacheManager : DefaultCacheManager? = null
	var cache : Cache<String, TestEntity>? = null
	var dao  : IspnDaoBase<TestEntity, String>? = null
	@Mock var eventListener : EventListener? = null

	@Before fun setup() {
		cacheManager = DefaultCacheManager()
		cacheManager!!.start()
		cache = cacheManager!!.getCache("test")
		dao = TestDao(cache!!, eventListener!!)
	}

	@After
	fun cleanup() {
		cacheManager?.stop()
	}

	@Test
	fun get() {
		val orig = TestEntity()
		cache!!.put("A", orig)
		val entity = dao!!.get("A")
		Assert.assertEquals(orig, entity)
	}
}