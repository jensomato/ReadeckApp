package de.readeckapp.domain.model

data class BookmarkCounts(
    val unread: Int = 0,
    val archived: Int = 0,
    val favorite: Int = 0,
    val article: Int = 0,
    val video: Int = 0,
    val picture: Int = 0,
    val total: Int = 0
)
