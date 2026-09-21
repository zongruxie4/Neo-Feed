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

package com.saulhdev.feeder.manager.nextcloud

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.saulhdev.feeder.data.source.NewsSourceRegistry
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

class NextcloudSyncWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val registry: NewsSourceRegistry by inject(NewsSourceRegistry::class.java)

    override suspend fun doWork(): Result {
        val accountId = inputData.getString(KEY_ACCOUNT_ID)
        Log.d(TAG, "Running NextcloudSyncWorker for account: $accountId")

        return try {
            if (!accountId.isNullOrBlank()) {
                val source = registry.getSourceForAccount(accountId)
                val res = source?.sync(context, forceNetwork = true)
                if (res?.success == true) Result.success() else Result.retry()
            } else {
                registry.syncAll(context, forceNetwork = true)
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in NextcloudSyncWorker: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "NextcloudSyncWorker"
        const val KEY_ACCOUNT_ID = "account_id"

        fun schedulePeriodicSync(
            workManager: WorkManager,
            accountId: String,
            intervalMinutes: Int,
            wifiOnly: Boolean = false,
        ) {
            val constraints = Constraints.Builder().apply {
                if (wifiOnly) {
                    setRequiredNetworkType(NetworkType.UNMETERED)
                } else {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }
            }.build()

            val interval = intervalMinutes.coerceAtLeast(15).toLong()
            val request = PeriodicWorkRequestBuilder<NextcloudSyncWorker>(
                interval, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_ACCOUNT_ID to accountId))
                .addTag("NextcloudSyncWorker")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "nextcloud_sync_$accountId",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelPeriodicSync(workManager: WorkManager, accountId: String) {
            workManager.cancelUniqueWork("nextcloud_sync_$accountId")
        }
    }
}
