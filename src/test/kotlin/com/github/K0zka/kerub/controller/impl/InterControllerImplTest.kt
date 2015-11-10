package com.github.K0zka.kerub.controller.impl

import com.github.K0zka.kerub.controller.EntityEventMessage
import com.github.K0zka.kerub.model.Project
import com.github.K0zka.kerub.model.messages.EntityAddMessage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.jms.core.JmsTemplate
import java.util.Date
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
public class InterControllerImplTest {
	@Mock
	var jmsTemplate: JmsTemplate? = null
	var interControllerImpl: InterControllerImpl? = null

	@Before
	fun setup() {
		interControllerImpl = InterControllerImpl(jmsTemplate!!)
	}

	@Test
	fun sendToControllerEntityEvent() {
		val controllerId = "test-controller"
		val message = EntityAddMessage(
				obj = Project(
						id = UUID.randomUUID(),
						created = Date(),
						description = "",
						expectations = listOf(),
						name = "test"
				             ),
				date = System.currentTimeMillis()
		                                       )
		interControllerImpl!!.sendToController(controllerId, EntityEventMessage(
				message
		                                                                       ))

	}

	@Test
	fun broadcast() {

		interControllerImpl!!.broadcast(EntityAddMessage(
				obj = Project(
						id = UUID.randomUUID(),
						created = Date(),
						description = "",
						expectations = listOf(),
						name = "test"
				             ),
				date = System.currentTimeMillis()
		                                                ))

	}
}