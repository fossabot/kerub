package com.github.K0zka.kerub.services

import javax.ws.rs.Path
import javax.ws.rs.GET
import java.util.UUID
import javax.ws.rs.PathParam
import com.github.K0zka.kerub.model.AuditEntry
import javax.ws.rs.Produces
import javax.ws.rs.Consumes
import org.apache.shiro.authz.annotation.RequiresAuthentication

Path("/audit")
Produces("application/json")
Consumes("application/json")
RequiresAuthentication
public trait AuditService {
	Path("/{id}")
	GET
	fun listById(PathParam("id") id : UUID) : List<AuditEntry>
}