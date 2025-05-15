package de.readeckapp.domain.model

data class BookmarkListItem(
    val id: String,
    val url: String,
    val title: String,
    val siteName: String,
    val isMarked: Boolean,
    val isArchived: Boolean,
    val isRead: Boolean,
    val thumbnailSrc: String,
    val iconSrc: String,
    val imageSrc: String,
    val labels: List<String>,
    val type: Bookmark.Type
)
