package com.saulhdev.feeder.manager.models

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.repository.FeedRepository
import com.saulhdev.feeder.manager.sync.requestFeedSync
import com.saulhdev.feeder.utils.extensions.ToastMaker
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
            val sourceRepository: FeedRepository by inject(FeedRepository::class.java)
            contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(
                    it,
                    sourceRepository.loadTags()
                ) { tag ->
                    sourceRepository.loadFeedsByTag(tag = tag)
                }
            }
        }
        Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to export OPML", e)
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
        Log.e("OMPL", "Failed to import OPML", e)
        val toastMaker: ToastMaker by inject(ToastMaker::class.java)
        toastMaker.makeToast(R.string.failed_to_import_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}