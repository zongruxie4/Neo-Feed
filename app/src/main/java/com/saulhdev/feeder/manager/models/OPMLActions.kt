/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
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

package com.saulhdev.feeder.manager.models

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.extensions.ToastMaker
import com.saulhdev.feeder.manager.sync.requestFeedSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import kotlin.system.measureTimeMillis

/**
 * Exports OPML on a background thread
 */
suspend fun exportOpml(uri: Uri) = withContext(Dispatchers.IO) {
    try {
        val time = measureTimeMillis {
            val contentResolver: ContentResolver by inject(ContentResolver::class.java)
            val sourceRepository: SourcesRepository by inject(SourcesRepository::class.java)
            contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(
                    it,
                    sourceRepository.getAllTags()
                ) { tag ->
                    sourceRepository.loadFeedsByTag(tag = tag)
                }
            }
        }
        Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OPML", "Failed to export OPML", e)
        val toastMaker: ToastMaker by inject(ToastMaker::class.java)
        toastMaker.makeToast(R.string.failed_to_export_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}

/**
 * Imports OPML on a background thread
 */
suspend fun importOpml(uri: Uri) = withContext(Dispatchers.IO) {
    val db: NeoFeedDb by inject(NeoFeedDb::class.java)
    try {
        val time = measureTimeMillis {
            val parser = OPMLParser(OPMLToRoom(db))
            val contentResolver: ContentResolver by inject(ContentResolver::class.java)
            contentResolver.openInputStream(uri).use {
                it?.let { stream ->
                    parser.parseInputStream(stream)
                }
            }
            requestFeedSync()
        }
        Log.d("OPML", "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OPML", "Failed to import OPML", e)
        val toastMaker: ToastMaker by inject(ToastMaker::class.java)
        toastMaker.makeToast(R.string.failed_to_import_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}
