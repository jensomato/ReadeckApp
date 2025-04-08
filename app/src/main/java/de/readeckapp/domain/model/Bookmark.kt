package de.readeckapp.domain.model

import kotlinx.datetime.LocalDateTime

data class Bookmark(
    val id: String,
    val href: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val state: Int,
    val loaded: Boolean,
    val url: String,
    val title: String,
    val siteName: String,
    val site: String,
    val authors: List<String>,
    val lang: String,
    val textDirection: String,
    val documentTpe: String,
    val type: Type,
    val hasArticle: Boolean,
    val description: String,
    val isDeleted: Boolean,
    val isMarked: Boolean,
    val isArchived: Boolean,
    val labels: List<String>,
    val readProgress: Int,
    val wordCount: Int?,
    val readingTime: Int?,
    val article: Resource,
    val articleContent: String?,
    val icon: ImageResource,
    val image: ImageResource,
    val log: Resource,
    val props: Resource,
    val thumbnail: ImageResource
) {
    fun isRead(): Boolean {
        return readProgress == 100
    }
    data class Resource(
        val src: String
    )
    data class ImageResource(
        val src: String,
        val width: Int,
        val height: Int
    )
    sealed class Type {
        data object Article: Type()
        data object Picture: Type()
        data object Video: Type()
    }
}
