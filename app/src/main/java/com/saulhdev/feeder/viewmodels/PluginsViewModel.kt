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

package com.saulhdev.feeder.viewmodels

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.data.account.AccountStorage
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.data.source.NewsSourceRegistry
import com.saulhdev.feeder.manager.mastodon.MastodonStorage
import com.saulhdev.feeder.manager.miniflux.MinifluxClient
import com.saulhdev.feeder.manager.nextcloud.NextcloudNewsClient
import com.saulhdev.feeder.manager.nextcloud.NextcloudSyncWorker
import com.saulhdev.feeder.utils.ResourceProvider
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PluginsUiState(
    val accounts: List<AccountConfig> = emptyList(),
    val isTestingConnection: Boolean = false,
    val testConnectionMessage: String? = null,
    val testConnectionSuccess: Boolean? = null,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

class PluginsViewModel(
    private val accountStorage: AccountStorage,
    private val nextcloudClient: NextcloudNewsClient,
    private val minifluxClient: MinifluxClient,
    private val registry: NewsSourceRegistry,
    private val workManager: WorkManager,
    private val sourcesRepo: SourcesRepository,
    private val mastodonStorage: MastodonStorage,
    private val res: ResourceProvider
) : NeoViewModel() {

    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountStorage.accountsFlow.collect { list ->
                _uiState.update { it.copy(accounts = list) }
            }
        }
    }

    fun testNextcloudConnection(serverUrl: String, username: String, token: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestingConnection = true,
                    testConnectionMessage = null,
                    testConnectionSuccess = null
                )
            }
            val result = nextcloudClient.checkConnection(serverUrl, username, token)

            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testConnectionSuccess = true,
                        testConnectionMessage = res.getString(R.string.connection_success) + (user?.userId
                            ?: username)
                    )
                }
            } else {
                val error =
                    result.exceptionOrNull()?.message ?: res.getString(R.string.connection_unknown)
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testConnectionSuccess = false,
                        testConnectionMessage = res.getString(R.string.connection_failed) + ": $error"
                    )
                }
            }
        }
    }

    fun testMinifluxConnection(serverUrl: String, apiToken: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestingConnection = true,
                    testConnectionMessage = null,
                    testConnectionSuccess = null
                )
            }
            val result = minifluxClient.checkConnection(serverUrl, apiToken)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testConnectionSuccess = true,
                        testConnectionMessage = res.getString(R.string.connection_success) + " ${user?.username ?: "Miniflux"}"
                    )
                }
            } else {
                val error =
                    result.exceptionOrNull()?.message ?: res.getString(R.string.connection_unknown)
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testConnectionSuccess = false,
                        testConnectionMessage = res.getString(R.string.connection_failed) + ": $error"
                    )
                }
            }
        }
    }

    fun clearTestConnectionState() {
        _uiState.update {
            it.copy(
                isTestingConnection = false,
                testConnectionMessage = null,
                testConnectionSuccess = null
            )
        }
    }

    fun saveAccount(account: AccountConfig, context: Context? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            accountStorage.saveAccount(account)
            if (account is AccountConfig.MastodonAccount) {
                val allSources = sourcesRepo.getAllSources()
                for (feed in allSources) {
                    if (feed.sourceType == "mastodon" && (feed.title.contains(account.instance) || feed.url.toString()
                            .contains(account.instance))
                    ) {
                        val updated = feed.copy(
                            isEnabled = account.isEnabled,
                            requireLink = account.requireLink,
                            requireImage = account.requireImage,
                            excludeReplies = account.excludeReplies
                        )
                        sourcesRepo.updateSource(updated)
                    }
                }
            } else if (account is AccountConfig.NextcloudNewsAccount) {
                if (account.backgroundSyncEnabled && account.isEnabled) {
                    NextcloudSyncWorker.schedulePeriodicSync(
                        workManager,
                        account.id,
                        account.syncIntervalMinutes
                    )
                } else {
                    NextcloudSyncWorker.cancelPeriodicSync(workManager, account.id)
                }
                context?.let { ctx ->
                    syncAccount(account, ctx)
                }
            } else if (account is AccountConfig.MinifluxAccount) {
                val allSources = sourcesRepo.getAllSources()
                for (feed in allSources) {
                    if (feed.sourceType == "miniflux") {
                        sourcesRepo.updateSource(feed.copy(isEnabled = account.isEnabled))
                    }
                }
                if (account.backgroundSyncEnabled && account.isEnabled) {
                    NextcloudSyncWorker.schedulePeriodicSync(
                        workManager,
                        account.id,
                        account.syncIntervalMinutes
                    )
                } else {
                    NextcloudSyncWorker.cancelPeriodicSync(workManager, account.id)
                }
                context?.let { ctx ->
                    syncAccount(account, ctx)
                }
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = accountStorage.getAccount(accountId)
            NextcloudSyncWorker.cancelPeriodicSync(workManager, accountId)
            if (account is AccountConfig.MastodonAccount) {
                val allSources = sourcesRepo.getAllSources()
                for (feed in allSources) {
                    if (feed.sourceType == "mastodon" && (feed.title.contains(account.instance) || feed.url.toString()
                            .contains(account.instance))
                    ) {
                        sourcesRepo.deleteFeed(feed.id)
                    }
                }
                mastodonStorage.deleteAccessToken(account.instance, account.username)
            } else if (account is AccountConfig.NextcloudNewsAccount) {
                val allSources = sourcesRepo.getAllSources()
                for (feed in allSources) {
                    if (feed.sourceType == "nextcloud_news") {
                        sourcesRepo.deleteFeed(feed.id)
                    }
                }
            } else if (account is AccountConfig.MinifluxAccount) {
                val allSources = sourcesRepo.getAllSources()
                for (feed in allSources) {
                    if (feed.sourceType == "miniflux") {
                        sourcesRepo.deleteFeed(feed.id)
                    }
                }
            }
            accountStorage.deleteAccount(accountId)
        }
    }

    fun toggleAccountEnabled(account: AccountConfig) {
        val updated = when (account) {
            is AccountConfig.NextcloudNewsAccount -> account.copy(isEnabled = !account.isEnabled)
            is AccountConfig.MastodonAccount -> account.copy(isEnabled = !account.isEnabled)
            is AccountConfig.MinifluxAccount -> account.copy(isEnabled = !account.isEnabled)
        }
        saveAccount(updated)
    }

    fun syncAccount(account: AccountConfig, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    statusMessage = context.getString(R.string.syncing) + " ${account.displayName}…"
                )
            }
            val source = registry.getSourceForAccount(account.id)
            val resource = source?.sync(context, forceNetwork = true)
            val msg = if (resource?.success == true) {
                res.getString(R.string.sync_completed) + " (${resource.itemsCount} )"
            } else {
                res.getString(R.string.sync_error) + " ${resource?.errorMessage}"
            }
            _uiState.update { it.copy(isSyncing = false, statusMessage = msg) }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
