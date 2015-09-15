package com.github.K0zka.kerub.exceptions.mappers

import com.github.K0zka.kerub.utils.getLogger
import org.apache.shiro.authc.AuthenticationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

public class AuthenticationExceptionMapper : ExceptionMapper<AuthenticationException> {
	companion object {
		private val logger = getLogger(AuthenticationExceptionMapper::class)
	}
	override fun toResponse(exception: AuthenticationException): Response {
		logger.debug("Not authenticated", exception)
		return Response.status(Response.Status.UNAUTHORIZED).entity(RestError("AUTH2", "Authentication needed")).build()
	}

}