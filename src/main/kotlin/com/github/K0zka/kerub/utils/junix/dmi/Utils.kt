package com.github.K0zka.kerub.utils.junix.dmi

fun String.substringBetween(prefix: String, postfix : String) : String =
		this.substringAfter(prefix, "").substringBefore(postfix, "")

fun String.intBetween(prefix: String, postfix : String) : Int =
		this.substringAfter(prefix).substringBefore(postfix).toInt()

fun String.optionalIntBetween(prefix: String, postfix : String) : Int? {
	try {
		return this.substringAfter(prefix).substringBefore(postfix).toInt()
	} catch (nfe : NumberFormatException) {
		return null
	}
}