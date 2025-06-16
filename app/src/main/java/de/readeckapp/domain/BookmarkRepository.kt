package de.readeckapp.domain

import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.model.BookmarkCounts
import kotlinx.coroutines.flow.Flow
import de.readeckapp.domain.model.BookmarkListItem

interface BookmarkRepository {
    fun observeBookmarks(
        type: Bookmark.Type? = null,
        unread: Boolean? = null,
        archived: Boolean? = null,
        favorite: Boolean? = null,
        state: Bookmark.State? = null
    ): Flow<List<Bookmark>>

    fun observeBookmarkListItems(
        type: Bookmark.Type? = null,
        unread: Boolean? = null,
        archived: Boolean? = null,
        favorite: Boolean? = null,
        state: Bookmark.State? = null
    ): Flow<List<BookmarkListItem>>

    suspend fun insertBookmarks(bookmarks: List<Bookmark>)
    suspend fun getBookmarkById(id: String): Bookmark
    fun observeBookmark(id: String): Flow<Bookmark?>
    suspend fun deleteAllBookmarks()
    suspend fun deleteBookmark(id: String): UpdateResult
    suspend fun createBookmark(title: String, url: String): String
    suspend fun updateBookmark(bookmarkId: String, isFavorite: Boolean?, isArchived: Boolean?, isRead: Boolean?): UpdateResult
    suspend fun performFullSync(): SyncResult
    fun observeAllBookmarkCounts(): Flow<BookmarkCounts>
    sealed class UpdateResult {
        data object Success: UpdateResult()
        data class Error(val errorMessage: String, val code: Int? = null, val ex: Exception? = null): UpdateResult()
        data class NetworkError(val errorMessage: String, val ex: Exception?): UpdateResult()
    }
    sealed class SyncResult {
        data class Success(val countDeleted: Int): SyncResult()
        data class Error(val errorMessage: String, val code: Int? = null, val ex: Exception? = null): SyncResult()
        data class NetworkError(val errorMessage: String, val ex: Exception?): SyncResult()
    }
}
