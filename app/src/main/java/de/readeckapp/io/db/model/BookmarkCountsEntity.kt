package de.readeckapp.io.db.model

import androidx.room.ColumnInfo

data class BookmarkCountsEntity(
    @ColumnInfo(name = "unread_count") val unread: Int,
    @ColumnInfo(name = "archived_count") val archived: Int,
    @ColumnInfo(name = "favorite_count") val favorite: Int,
    @ColumnInfo(name = "article_count") val article: Int,
    @ColumnInfo(name = "video_count") val video: Int,
    @ColumnInfo(name = "picture_count") val picture: Int,
    @ColumnInfo(name = "total_count") val total: Int
)
