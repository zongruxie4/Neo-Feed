package com.saulhdev.feeder.models

data class Feed(
    val version: String? = "https://jsonfeed.org/version/1.1",
    val title: String?,
    val home_page_url: String? = null,
    val feed_url: String? = null,
    val description: String? = null,
    val user_comment: String? = null,
    val next_url: String? = null,
    val icon: String? = null,
    val favicon: String? = null,
    val author: Author? = null,
    val expired: Boolean? = null,
    val hubs: List<Hub>? = null,
    val items: List<Item>?
)

data class Author(
    val name: String? = null,
    val url: String? = null,
    val avatar: String? = null
)

data class Item(
    val id: String?,
    val url: String? = null,
    val external_url: String? = null,
    val title: String? = null,
    val content_html: String? = null,
    val content_text: String? = null,
    val summary: String? = null,
    val image: String? = null,
    val banner_image: String? = null,
    val date_published: String? = null,
    val date_modified: String? = null,
    val author: Author? = null,
    val tags: List<String>? = null,
    val attachments: List<Attachment>? = null
)

data class Attachment(
    val url: String?,
    val mime_type: String? = null,
    val title: String? = null,
    val size_in_bytes: Long? = null,
    val duration_in_seconds: Long? = null
)

data class Hub(
    val type: String?,
    val url: String?
)
