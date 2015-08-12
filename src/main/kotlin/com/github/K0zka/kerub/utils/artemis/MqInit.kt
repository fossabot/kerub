package com.github.K0zka.kerub.utils.artemis

import com.github.K0zka.kerub.host.ControllerManager
import com.github.K0zka.kerub.utils.getLogger
import com.github.K0zka.kerub.utils.use
import org.apache.activemq.artemis.api.core.SimpleString
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS
import javax.jms.ConnectionFactory
import kotlin.platform.platformStatic

/**
 * This class (and package) is only needed because HornetQ/Artemis does not support auto-creation of destinations.
 * https://issues.jboss.org/browse/HORNETQ-302
 */
public object MqInit {
	val logger = getLogger(MqInit::class)

	platformStatic
	fun init(controllerManager: ControllerManager, artemis: EmbeddedJMS, factory : ConnectionFactory) : ConnectionFactory {
		val controllerId = controllerManager.getControllerId()
		val server = artemis.getActiveMQServer()
		val mqName = "kerub-mq-${controllerId}"
		server.deployQueue(SimpleString(mqName), SimpleString(mqName), null, true, false)
		factory.createConnection().use {
			it.createSession().use {
				it.createQueue(mqName)
			}
		}
		return factory
	}
}