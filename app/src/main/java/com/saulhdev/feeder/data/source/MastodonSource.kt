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
import com.saulhdev.feeder.data.account.AccountType
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.mastodon.MastodonFeedSync
import kotlin.time.Clock

class MastodonSource(
    private val account: AccountConfig.MastodonAccount,
    private val sourcesRepo: SourcesRepository,
    private val articleRepo: ArticleRepository,
) : NewsSource {

    override val accountId: String = account.id
    override val accountType: AccountType = AccountType.MASTODON
    override val displayName: String = "${account.username}@${account.instance}"

    override suspend fun sync(context: Context, forceNetwork: Boolean): SyncResult {
        return try {
            val allSources = sourcesRepo.getAllSources()
            val mastodonFeeds = allSources.filter {
                it.sourceType == "mastodon" &&
                        (it.title.contains(account.instance) || it.url.toString()
                            .contains(account.instance))
            }

            var syncedCount = 0
            val downloadTime = Clock.System.now()

            for (feed in mastodonFeeds) {
                if (!feed.isEnabled) continue
                sourcesRepo.setCurrentlySyncingOn(feed.id, true)
                try {
                    MastodonFeedSync.sync(
                        context = context,
                        articleRepo = articleRepo,
                        feedSql = feed,
                        filesDir = context.filesDir,
                        downloadTime = downloadTime
                    )
                    sourcesRepo.setCurrentlySyncingOn(feed.id, false, downloadTime)
                    syncedCount++
                } catch (e: Throwable) {
                    sourcesRepo.setCurrentlySyncingOn(feed.id, false)
                }
            }

            SyncResult(success = true, itemsCount = syncedCount)
        } catch (e: Exception) {
            SyncResult(success = false, errorMessage = e.message)
        }
    }
}
