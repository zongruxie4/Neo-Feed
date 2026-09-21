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

package com.saulhdev.feeder.data.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    NEXTCLOUD_NEWS,
    MASTODON,
    MINIFLUX,
}

@Serializable
sealed class AccountConfig {
    abstract val id: String
    abstract val accountType: AccountType
    abstract val displayName: String
    abstract val isEnabled: Boolean
    abstract val syncIntervalMinutes: Int
    abstract val backgroundSyncEnabled: Boolean
    abstract val lastSyncTimestamp: Long

    @Serializable
    @SerialName("NEXTCLOUD_NEWS")
    data class NextcloudNewsAccount(
        override val id: String,
        override val displayName: String,
        override val isEnabled: Boolean = true,
        override val syncIntervalMinutes: Int = 30,
        override val backgroundSyncEnabled: Boolean = true,
        override val lastSyncTimestamp: Long = 0L,
        val serverUrl: String,
        val username: String,
        val passwordOrToken: String,
        val excludedFolders: Set<String> = emptySet(),
    ) : AccountConfig() {
        override val accountType: AccountType = AccountType.NEXTCLOUD_NEWS
    }

    @Serializable
    @SerialName("MASTODON")
    data class MastodonAccount(
        override val id: String,
        override val displayName: String = "",
        override val isEnabled: Boolean = true,
        override val syncIntervalMinutes: Int = 30,
        override val backgroundSyncEnabled: Boolean = true,
        override val lastSyncTimestamp: Long = 0L,
        val instance: String,
        val username: String,
        val token: String = "",
        val tagsToFollow: List<String> = emptyList(),
        val requireLink: Boolean = true,
        val requireImage: Boolean = true,
        val excludeReplies: Boolean = true,
    ) : AccountConfig() {
        override val accountType: AccountType = AccountType.MASTODON
    }


    @Serializable
    @SerialName("MINIFLUX")
    data class MinifluxAccount(
        override val id: String,
        override val displayName: String,
        override val isEnabled: Boolean = true,
        override val syncIntervalMinutes: Int = 60,
        override val backgroundSyncEnabled: Boolean = true,
        override val lastSyncTimestamp: Long = 0L,
        val serverUrl: String = "",
        val apiToken: String = "",
        val apiType: String = "GoogleReader", // "GoogleReader" or "Fever"
    ) : AccountConfig() {
        override val accountType: AccountType = AccountType.MINIFLUX
    }
}
