package com.github.K0zka.kerub.security

import com.github.K0zka.kerub.data.AssetDao
import com.github.K0zka.kerub.model.Asset
import com.github.K0zka.kerub.model.paging.SearchResultPage
import com.github.K0zka.kerub.model.paging.SortResultPage
import java.util.UUID

interface AccessController {
	fun <T> doAsAccountMember(accountId: UUID, action: () -> T): T
	/**
	 * Pre-check the permission before
	 *
	 * Use for insert operations
	 */
	fun <T> checkAndDo(asset: Asset, action: () -> T?): T?
	/**
	 * Perform the action and check if the result is readable by the current user.
	 * If user does not have permission, SecurityException should be thrown
	 * If the action returns null, null is returned
	 *
	 * Use for reading records by ID, etc.
	 */
	fun <T : Asset> doAndCheck(action: () -> T?): T?

	/**
	 *
	 */
	fun <T : Asset> listWithFilter(dao: AssetDao<T>,
								   start: Long = 0,
								   limit: Int = Int.MAX_VALUE,
								   sort: String = "id"): SortResultPage<T>

	/**
	 *
	 */
	fun <T : Asset> searchWithFilter(
			dao : AssetDao<T>,
			field: String,
			value: String,
			start: Long = 0,
			limit: Int = Int.MAX_VALUE
	) : SearchResultPage<T>
}