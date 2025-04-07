package de.readeckapp.io.rest.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkDto(
    val id: String,
    val href: String,
    val created: Instant,
    val updated: Instant,
    val state: Int,
    val loaded: Boolean,
    val url: String,
    val title: String,
    @SerialName("site_name")
    val siteName: String,
    val site: String,
    val authors: List<String>,
    val lang: String,
    @SerialName("text_direction")
    val textDirection: String,
    @SerialName("document_type")
    val documentTpe: String,
    val type: String,
    @SerialName("has_article")
    val hasArticle: Boolean,
    val description: String,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("is_marked")
    val isMarked: Boolean,
    @SerialName("is_archived")
    val isArchived: Boolean,
    val labels: List<String>,
    @SerialName("read_progress")
    val readProgress: Int? = null,
    val resources: Resources,
    @SerialName("word_count")
    val wordCount: Int? = null,
    @SerialName("reading_time")
    val readingTime: Int? = null
)

@Serializable
data class Resources(
    val article: Resource? = null,
    val icon: ImageResource? = null,
    val image: ImageResource? = null,
    val log: Resource,
    val props: Resource,
    val thumbnail: ImageResource? = null
)

@Serializable
data class Resource(
    val src: String
)

@Serializable
data class ImageResource(
    val src: String,
    val width: Int,
    val height: Int
)
