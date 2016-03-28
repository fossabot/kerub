package com.github.K0zka.kerub.model.expectations

import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.K0zka.kerub.model.ExpectationLevel

@JsonTypeName("storage-availability")
data class StorageAvailabilityExpectation(override val level: ExpectationLevel = ExpectationLevel.DealBreaker) : VirtualStorageExpectation
