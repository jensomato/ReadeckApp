package de.readeckapp.domain

import de.readeckapp.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getAllBookmarks(
        type: Bookmark.Type? = null,
        unread: Boolean? = null,
        archived: Boolean? = null,
        favorite: Boolean? = false
    ): Flow<List<Bookmark>>

    suspend fun insertBookmarks(bookmarks: List<Bookmark>)
    suspend fun getBookmarkById(id: String): Bookmark
}