package com.github.kerubistan.kerub.model.expectations

import com.github.kerubistan.kerub.model.ExpectationLevel

/**
 * A VirtualMachineExpectation applied on all the VM's in the pool.
 */
data class PoolAllVmExpectation(val vmExpectation: VirtualMachineExpectation,
								override val level: ExpectationLevel = vmExpectation.level) : PoolExpectation