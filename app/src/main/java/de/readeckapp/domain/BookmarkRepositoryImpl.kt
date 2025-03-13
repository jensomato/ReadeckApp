package de.readeckapp.domain

import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.domain.mapper.toEntity
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.CreateBookmarkDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val readeckApi: ReadeckApi
) : BookmarkRepository {
    override fun observeBookmarks(
        type: Bookmark.Type?,
        unread: Boolean?,
        archived: Boolean?,
        favorite: Boolean?
    ): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByFilters(
            type = type?.let {
                when (it) {
                    Bookmark.Type.Article -> BookmarkEntity.Type.Article
                    Bookmark.Type.Picture -> BookmarkEntity.Type.Picture
                    Bookmark.Type.Video -> BookmarkEntity.Type.Video
                }
            },
            isUnread = unread,
            isArchived = archived,
            isFavorite = favorite
        ).map { bookmarks -> bookmarks.map { it.toDomain() } }
    }

    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {
        bookmarkDao.insertBookmarks(bookmarks.map { it.toEntity() })
    }

    override suspend fun getBookmarkById(id: String): Bookmark {
        return bookmarkDao.getBookmarkById(id).toDomain()
    }

    override fun observeBookmark(id: String): Flow<Bookmark> {
        return bookmarkDao.observeBookmark(id).map { it.toDomain() }
    }

    override suspend fun deleteAllBookmarks() {
        bookmarkDao.deleteAllBookmarks()
    }

    override suspend fun createBookmark(title: String, url: String): String {
        val createBookmarkDto = CreateBookmarkDto(title = title, url = url)
        val response = readeckApi.createBookmark(createBookmarkDto)
        if (response.isSuccessful) {
            return response.headers()[ReadeckApi.Header.BOOKMARK_ID]!!
        } else {
            throw Exception("Failed to create bookmark")
        }
    }
}
