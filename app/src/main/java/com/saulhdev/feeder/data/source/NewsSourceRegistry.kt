/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.data.source

import android.content.Context
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.data.account.AccountStorage
import com.saulhdev.feeder.data.db.dao.SyncQueueDao
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.miniflux.MinifluxClient
import com.saulhdev.feeder.manager.nextcloud.NextcloudNewsClient

class NewsSourceRegistry(
    private val accountStorage: AccountStorage,
    private val nextcloudClient: NextcloudNewsClient,
    private val minifluxClient: MinifluxClient,
    private val syncQueueDao: SyncQueueDao,
    private val sourcesRepo: SourcesRepository,
    private val articleRepo: ArticleRepository
) {

    fun getSources(): List<NewsSource> {
        val accounts = accountStorage.loadAccounts()
        return accounts.mapNotNull { account ->
            if (!account.isEnabled) return@mapNotNull null
            createNewsSource(account)
        }
    }

    fun getSourceForAccount(accountId: String): NewsSource? {
        val account = accountStorage.getAccount(accountId) ?: return null
        return createNewsSource(account)
    }

    fun getSourceForFeed(feed: Feed): NewsSource? {
        val sources = getSources()
        return when (feed.sourceType) {
            "mastodon" -> sources.filterIsInstance<MastodonSource>().firstOrNull()
            "nextcloud_news" -> sources.filterIsInstance<NextcloudNewsSource>().firstOrNull()
            "miniflux" -> sources.filterIsInstance<MinifluxSource>().firstOrNull()
            else -> null
        }
    }

    suspend fun syncAll(context: Context, forceNetwork: Boolean): Map<String, SyncResult> {
        val results = mutableMapOf<String, SyncResult>()
        val sources = getSources()
        for (source in sources) {
            val result = source.sync(context, forceNetwork)
            results[source.accountId] = result
        }
        return results
    }

    private fun createNewsSource(account: AccountConfig): NewsSource {
        return when (account) {
            is AccountConfig.NextcloudNewsAccount -> NextcloudNewsSource(
                account = account,
                client = nextcloudClient,
                syncQueueDao = syncQueueDao,
                sourcesRepo = sourcesRepo,
                articleRepo = articleRepo,
            )

            is AccountConfig.MastodonAccount -> MastodonSource(
                account = account,
                sourcesRepo = sourcesRepo,
                articleRepo = articleRepo,
            )

            is AccountConfig.MinifluxAccount -> MinifluxSource(
                account = account,
                client = minifluxClient,
                syncQueueDao = syncQueueDao,
                sourcesRepo = sourcesRepo,
                articleRepo = articleRepo,
            )
        }
    }
}
