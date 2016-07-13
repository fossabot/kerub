package com.github.K0zka.kerub.utils.junix.storagemanager.gvinum

import java.io.Serializable

data class GvinumPlex (
		val name: String,
		val up : Boolean,
		val subdisks : Int,
		val volume : String
) : Serializable