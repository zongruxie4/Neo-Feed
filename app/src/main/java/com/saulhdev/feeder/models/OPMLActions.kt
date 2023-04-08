package com.saulhdev.feeder.models

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.saulhdev.feeder.R
import com.saulhdev.feeder.db.FeedDao
import com.saulhdev.feeder.db.NeoFeedDb
import com.saulhdev.feeder.sync.requestFeedSync
import com.saulhdev.feeder.utils.ToastMaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.system.measureTimeMillis

/**
 * Exports OPML on a background thread
 */
suspend fun exportOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    try {
        val time = measureTimeMillis {
            val contentResolver: ContentResolver by di.instance()
            val feedDao: FeedDao by di.instance()
            contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(
                    it,
                    feedDao.loadTags()
                ) { tag ->
                    feedDao.loadFeeds(tag = tag)
                }
            }
        }
        Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to export OPML", e)
        val toastMaker = di.direct.instance<ToastMaker>()
        toastMaker.makeToast(R.string.failed_to_export_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}

/**
 * Imports OPML on a background thread
 */
suspend fun importOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    val db: NeoFeedDb by di.instance()
    try {
        val time = measureTimeMillis {
            val parser = OpmlParser(OPMLToRoom(db))
            val contentResolver: ContentResolver by di.instance()
            contentResolver.openInputStream(uri).use {
                it?.let { stream ->
                    parser.parseInputStream(stream)
                }
            }
            requestFeedSync(di = di)
        }
        Log.d("OPML", "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to import OPML", e)
        val toastMaker = di.direct.instance<ToastMaker>()
        toastMaker.makeToast(R.string.failed_to_import_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}