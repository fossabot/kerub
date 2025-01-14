package com.github.kerubistan.kerub.model.expectations

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.kerubistan.kerub.model.ExpectationLevel

/**
 * Set the minimum and optionally the maximum number of virtual machines running in the pool.
 */
@JsonTypeName("pool-running-vms")
data class PoolRunningVmsExpectation @JsonCreator constructor(val min: Int = 0,
									 val max: Int? = null,
									 override val level: ExpectationLevel = ExpectationLevel.Want) : PoolExpectation {
	init {
		check(min >= 0) { "Minimal amount of VMS ($min) can not be less than 0" }
		check(max?.let { it > min } ?: true) { "maximal amount ($max) must be more than minimum ($min)" }
	}
}