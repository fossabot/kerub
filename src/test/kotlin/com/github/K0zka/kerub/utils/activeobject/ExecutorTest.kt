package com.github.K0zka.kerub.utils.activeobject

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.context.ApplicationContext

@RunWith(MockitoJUnitRunner::class)
public class ExecutorTest {
	@Mock
	var appCtx: ApplicationContext? = null

	@Mock
	var service : HelloService? = null

	var executor: Executor? = null

	@Before
	fun setup() {
		executor = Executor()
		executor!!.setApplicationContext(appCtx)
		Mockito.`when`(appCtx!!.getBean("helloService"))!!.thenReturn(service)
	}

	interface HelloService {
		fun hello(name : String)
	}

	@Test
	fun execute() {
		executor!!.execute(
				AsyncInvocation(
						beanName = "helloService",
						args = listOf("world"),
						methodName = "hello",
						paramTypes = listOf(String::class.java)))
		Mockito.verify(service)!!.hello("world")
	}
}