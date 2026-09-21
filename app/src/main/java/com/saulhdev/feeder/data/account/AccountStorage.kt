/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the GNU General Public License as
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

package com.saulhdev.feeder.data.account

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.saulhdev.feeder.data.db.dao.FeedSourceDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Suppress("DEPRECATION")
class AccountStorage(
    context: Context,
    private val feedSourceDao: FeedSourceDao,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "account_class"
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    private val _accountsFlow = MutableStateFlow<List<AccountConfig>>(emptyList())
    val accountsFlow: StateFlow<List<AccountConfig>> = _accountsFlow.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            discoverLegacyAccounts()
            loadAccounts()
        }
    }

    private suspend fun discoverLegacyAccounts() {
        try {
            val accounts = loadAccounts()
            val mastodonAccounts = accounts.filterIsInstance<AccountConfig.MastodonAccount>()
            val legacyFeeds = feedSourceDao.loadFeeds().filter { it.sourceType == "mastodon" }
            for (feed in legacyFeeds) {
                val instance = feed.url.host.orEmpty().ifBlank { feed.title.substringAfter("@") }
                val username = feed.title.substringBefore("@")
                val accountId = "mastodon_${instance}_$username"
                if (mastodonAccounts.none { it.id == accountId || (it.instance == instance && it.username == username) }) {
                    val legacyAccount = AccountConfig.MastodonAccount(
                        id = accountId,
                        displayName = "$username@$instance",
                        instance = instance,
                        username = username,
                        isEnabled = feed.isEnabled,
                        backgroundSyncEnabled = true,
                        requireLink = feed.requireLink,
                        requireImage = feed.requireImage,
                        excludeReplies = feed.excludeReplies,
                    )
                    saveAccountInternal(legacyAccount)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun loadAccounts(): List<AccountConfig> {
        val rawList = prefs.getStringSet(KEY_ACCOUNT_IDS, emptySet()) ?: emptySet()
        val accounts = mutableListOf<AccountConfig>()

        for (id in rawList) {
            if (id == "local_rss_account") {
                // Remove legacy local RSS account from configured external plugins list
                val currentIds = rawList.toMutableSet()
                currentIds.remove(id)
                prefs.edit { putStringSet(KEY_ACCOUNT_IDS, currentIds).remove(accountKey(id)) }
                continue
            }
            val accountJson = prefs.getString(accountKey(id), null) ?: continue
            try {
                var account = json.decodeFromString<AccountConfig>(accountJson)
                if (account is AccountConfig.MastodonAccount && account.displayName.isBlank()) {
                    account = account.copy(displayName = "${account.username}@${account.instance}")
                }
                accounts.add(account)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _accountsFlow.value = accounts
        return accounts
    }

    fun saveAccount(account: AccountConfig) {
        saveAccountInternal(account)
        loadAccounts()
    }

    private fun saveAccountInternal(account: AccountConfig) {
        val serialized = json.encodeToString(account)
        val currentIds =
            prefs.getStringSet(KEY_ACCOUNT_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentIds.add(account.id)

        prefs.edit {
            putStringSet(KEY_ACCOUNT_IDS, currentIds)
                .putString(accountKey(account.id), serialized)
        }
    }

    fun getAccount(id: String): AccountConfig? {
        val accountJson = prefs.getString(accountKey(id), null) ?: return null
        return try {
            json.decodeFromString<AccountConfig>(accountJson)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteAccount(id: String) {
        val currentIds =
            prefs.getStringSet(KEY_ACCOUNT_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentIds.remove(id)

        prefs.edit {
            putStringSet(KEY_ACCOUNT_IDS, currentIds)
                .remove(accountKey(id))
        }

        loadAccounts()
    }

    private fun accountKey(id: String) = "account_data_$id"

    companion object {
        private const val PREFS_FILE = "neo_feed_accounts"
        private const val KEY_ACCOUNT_IDS = "configured_account_ids"
    }
}
