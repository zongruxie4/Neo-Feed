/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.sync

import android.content.Context
import com.saulhdev.feeder.db.Converters
import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedArticle
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.models.FeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RssClientSync(val context: Context) {
    suspend fun getArticleList(feed: Feed) {
        val parser = FeedParser()
        val repository = FeedRepository(context)
        val articles = arrayListOf<FeedArticle>()
        val converters = Converters()

        withContext(Dispatchers.IO) {
            parser.parseFeedUrl(feed.url)?.items?.filter {
                converters.dateTimeFromString(it.date_published)!!.toInstant() > feed.lastSync
            }?.forEach {
                articles.add(
                    FeedArticle(
                        guid = it.id.toString(),
                        title = it.title ?: "",
                        description = it.summary ?: "",
                        content_html = it.content_html ?: "",
                        imageUrl = it.image,
                        link = it.url,
                        feedId = feed.id,
                        pubDate = it.date_published.toString(),
                        categories = arrayListOf() //TODO it.tags,
                    )
                )
            }
            repository.updateOrInsertArticle(articles)
        }
    }
}