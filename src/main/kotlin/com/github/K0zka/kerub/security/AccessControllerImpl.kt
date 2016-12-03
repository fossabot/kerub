package com.github.K0zka.kerub.security


import com.github.K0zka.kerub.data.AccountMembershipDao
import com.github.K0zka.kerub.data.AssetDao
import com.github.K0zka.kerub.data.ControllerConfigDao
import com.github.K0zka.kerub.data.ProjectmembershipDao
import com.github.K0zka.kerub.model.Asset
import com.github.K0zka.kerub.model.AssetOwner
import com.github.K0zka.kerub.model.AssetOwnerType
import com.github.K0zka.kerub.model.paging.SearchResultPage
import com.github.K0zka.kerub.model.paging.SortResultPage
import com.github.K0zka.kerub.utils.join
import nl.komponents.kovenant.task
import org.apache.shiro.SecurityUtils.getSubject
import java.util.UUID

class AccessControllerImpl(
		private val controllerConfigDao: ControllerConfigDao,
		private val accountMembershipDao: AccountMembershipDao,
		private val projectmembershipDao: ProjectmembershipDao,
		private val validator: Validator
) : AccessController {

	override fun <T : Asset> listWithFilter(dao: AssetDao<T>, start: Long, limit: Int, sort: String): SortResultPage<T> {
		return if (filterNotRequired()) {
			val list = dao.list(start, limit, sort)
			SortResultPage(
					start = start,
					count = list.size.toLong(),
					result = list,
					sortBy = sort,
					total = list.size.toLong() // TODO
			)
		} else {
			val list = dao.listByOwners(
					owners = memberships(getSubject().principal.toString()),
					limit = limit,
					sort = sort,
					start = start
			)
			SortResultPage(
					start = start,
					count = list.size.toLong(),
					result = list,
					sortBy = sort,
					total = list.size.toLong() // TODO
			)
		}
	}

	internal fun memberships(userName: String)
			= listOf(
			task() {
				accountMembershipDao.listByUsername(userName).map {
					AssetOwner(ownerId = it.groupId, ownerType = AssetOwnerType.account)
				}
			},
			task() {
				projectmembershipDao.listByUsername(userName).map {
					AssetOwner(ownerId = it.groupId, ownerType = AssetOwnerType.project)
				}
			}
	).map { it.get() }.join()


	override fun <T : Asset> searchWithFilter(
			dao: AssetDao<T>,
			field: String,
			value: String,
			start: Long,
			limit: Int
	): SearchResultPage<T> {
		if (filterNotRequired()) {
			val list = dao.fieldSearch(field, value, start, limit)
			return SearchResultPage(
					count = list.size.toLong(),
					result = list,
					searchby = field,
					start = start.toLong(),
					total = dao.count().toLong()
			)
		} else {
			val assetOwners = memberships(getSubject().principal.toString()).toSet()
			val results = dao.fieldSearch(assetOwners, field, value, start, limit)
			return SearchResultPage(
					count = results.size.toLong(),
					result = results,
					searchby = field,
					start = start.toLong(),
					total = results.size.toLong() //TODO
			)
		}
	}

	/**
	 * Filtering of assets not required when the user is an administrator
	 * OR account are optional and therefore all users can see all VMs
	 */
	private fun filterNotRequired() = getSubject().hasRole(admin) || !controllerConfigDao.get().accountsRequired

	override fun <T : Asset> doAndCheck(action: () -> T?): T? {
		val result = action()

		return if (result == null) {
			null
		} else {
			return checkMembership(result)
		}
	}

	private fun <T : Asset> checkMembership(result: T): T {
		val owner = result.owner
		return if (owner == null) {
			result
		} else when (owner.ownerType) {
			AssetOwnerType.account -> doAsAccountMember(owner.ownerId) { result }
			else -> TODO()
		}
	}

	override fun <T> checkAndDo(asset: Asset, action: () -> T?): T? {
		val owner = asset.owner
		validator.validate(asset)
		val config = controllerConfigDao.get()
		return if (owner != null) {
			if (owner.ownerType == AssetOwnerType.account) {
				doAsAccountMember(owner.ownerId) {
					action()
				}
			} else {
				TODO("handle projects here")
			}
		} else {
			if (config.accountsRequired) {
				throw IllegalArgumentException("accounts required")
			} else {
				action()
			}
		}
	}

	override fun <T> doAsAccountMember(accountId: UUID, action: () -> T): T {
		if (getSubject().hasRole(admin)
				|| accountMembershipDao.isAccountMember(getSubject().principal.toString(), accountId)) {
			return action()
		} else {
			throw SecurityException("No permission")
		}
	}
}