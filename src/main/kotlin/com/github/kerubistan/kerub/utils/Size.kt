package com.github.kerubistan.kerub.utils

import java.math.BigDecimal
import java.math.BigInteger

val sizeMultiplier = BigDecimal("1024")

val KB = sizeMultiplier
val MB = KB * sizeMultiplier
val GB = MB * sizeMultiplier
val TB = GB * sizeMultiplier
val PB = TB * sizeMultiplier
val EB = PB * sizeMultiplier
val ZB = EB * sizeMultiplier
val YB = ZB * sizeMultiplier

val sizePostfixes = mapOf(
		"BYTES" to { l: BigDecimal -> l },
		"BYTE" to { l: BigDecimal -> l },
		"B" to { l: BigDecimal -> l },
		"K" to { l: BigDecimal -> l * KB },
		"KB" to { l: BigDecimal -> l * KB },
		"M" to { l: BigDecimal -> l * MB },
		"MB" to { l: BigDecimal -> l * MB },
		"G" to { l: BigDecimal -> l * GB },
		"GB" to { l: BigDecimal -> l * GB },
		"T" to { l: BigDecimal -> l * TB },
		"TB" to { l: BigDecimal -> l * TB },
		"P" to { l: BigDecimal -> l * PB },
		"PB" to { l: BigDecimal -> l * PB })

val numberRegex = "\\d+(\\.\\d+)?".toRegex()

fun parseStorageSize(storageSize: String): BigInteger {
	val unit = storageSize.replace(numberRegex, "").trim()
	val num = BigDecimal(storageSize.substringBefore(unit).trim())
	val fn = requireNotNull(sizePostfixes[unit.toUpperCase()]) { "Unknown storage unit $unit in $storageSize" }
	return fn(num).toBigInteger()
}

fun String.toSize() =
		parseStorageSize(this).apply {
			validateSize("size")
		}

fun BigInteger.validateSize(name : String) {
	check(this >= BigInteger.ZERO) {
		"$name ($this) must be greater or equal to zero"
	}
}