package com.github.K0zka.kerub.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

fun createObjectMapper() : ObjectMapper {
	return ObjectMapper().registerModule( KotlinModule() )
}
