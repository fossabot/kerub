package com.github.kerubistan.kerub.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

inline fun Logger.doOrLog(pattern: String, vararg data : Any, action : () -> Unit) {
	var pass = false
	try {
		action()
		pass = true
	} finally {
		if(!pass && isWarnEnabled) {
			this.warn(String.format(pattern, *data))
		}
	}
}

fun getLogger(cl: KClass<*>): Logger {
	return LoggerFactory.getLogger(cl.java)!!
}

fun getLogger(logger: String): Logger {
	return LoggerFactory.getLogger(logger)!!
}

fun <T : Any> justToString(data: T): Any = data

fun <T : Any> json(data: T) = jacksonObjectMapper().writeValueAsString(data)

fun <T : Any> Logger.logAndReturn(
		level: LogLevel = LogLevel.Debug,
		message: String = "",
		data: T,
		prettyPrint: (T) -> Any = ::justToString
) = data.let {
	when (level) {
		LogLevel.Debug -> this.debug(message, prettyPrint(data))
		LogLevel.Info -> this.info(message, prettyPrint(data))
		LogLevel.Warning -> this.warn(message, prettyPrint(data))
		LogLevel.Error -> this.error(message, prettyPrint(data))
		LogLevel.Off -> {
			//do nothing
		}
	}
	data
}

/**
 * Evaluate the lazies only if the debug is enabled.
 */
fun Logger.debugLazy(pattern: String, vararg lazies: Lazy<*>) {
	if (this.isDebugEnabled) {
		this.debug(pattern, * lazies.map { it.value }.toTypedArray())
	}
}