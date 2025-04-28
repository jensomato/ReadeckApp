package de.readeckapp.io.db.model

data class BookmarkListItemEntity(
    val id: String,
    val title: String,
    val siteName: String,
    val isMarked: Boolean,
    val isArchived: Boolean,
    val readProgress: Int,
    val thumbnailSrc: String,
    val imageSrc: String,
    val iconSrc: String,
    val labels: List<String>,
    val type: BookmarkEntity.Type
)
