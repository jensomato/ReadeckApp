package de.readeckapp.domain

import de.readeckapp.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeBookmarks(
        type: Bookmark.Type? = null,
        unread: Boolean? = null,
        archived: Boolean? = null,
        favorite: Boolean? = null
    ): Flow<List<Bookmark>>

    suspend fun insertBookmarks(bookmarks: List<Bookmark>)
    suspend fun getBookmarkById(id: String): Bookmark
    fun observeBookmark(id: String): Flow<Bookmark>
    suspend fun deleteAllBookmarks()
    suspend fun createBookmark(title: String, url: String): String
    suspend fun updateBookmark(bookmarkId: String, isFavorite: Boolean?, isArchived: Boolean?): UpdateResult
    sealed class UpdateResult {
        data object Success: UpdateResult()
        data class Error(val errorMessage: String, val code: Int? = null, val ex: Exception? = null): UpdateResult()
        data class NetworkError(val errorMessage: String): UpdateResult()
    }
}
