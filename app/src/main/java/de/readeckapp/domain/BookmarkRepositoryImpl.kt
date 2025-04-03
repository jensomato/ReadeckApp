package de.readeckapp.domain

import de.readeckapp.coroutine.IoDispatcher
import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.domain.mapper.toEntity
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.CreateBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkErrorDto
import de.readeckapp.io.rest.model.StatusMessageDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val readeckApi: ReadeckApi,
    private val json: Json,
    @IoDispatcher
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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

    override suspend fun updateBookmark(bookmarkId: String, isFavorite: Boolean?, isArchived: Boolean?): BookmarkRepository.UpdateResult {
        return withContext(dispatcher) {
            try {
                val response =
                    readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite, isArchived = isArchived))
                if (response.isSuccessful) {
                    Timber.i("Update Bookmark successful")
                    BookmarkRepository.UpdateResult.Success
                } else {
                    Timber.w("Error while Update Bookmark [code=${response.code()}, body=${response.errorBody()}]")
                    val errorBodyString = response.errorBody()?.string()
                    when (response.code()) {
                        422 -> {
                            if (!errorBodyString.isNullOrBlank()) {
                                try {
                                    json.decodeFromString<EditBookmarkErrorDto>(errorBodyString).let {
                                        BookmarkRepository.UpdateResult.Error(it.errors.toString(), response.code())
                                    }
                                } catch (e: SerializationException) {
                                    Timber.e(e, "Failed to parse error: ${e.message}")
                                    BookmarkRepository.UpdateResult.Error("Failed to parse error: ${e.message}", response.code(), e)
                                }
                            } else {
                                Timber.e("Empty error body")
                                BookmarkRepository.UpdateResult.Error("Empty error body", response.code())
                            }
                        }
                        else -> {
                            val errorState: StatusMessageDto = if (!errorBodyString.isNullOrBlank()) {
                                try {
                                    json.decodeFromString<StatusMessageDto>(errorBodyString)
                                } catch (e: SerializationException) {
                                    Timber.e(e, "Failed to parse error: ${e.message}")
                                    StatusMessageDto(
                                        response.code(),
                                        "Failed to parse error: ${e.message}"
                                    )
                                }
                            } else {
                                Timber.e("Empty error body")
                                StatusMessageDto(response.code(), "Empty error body")
                            }
                            BookmarkRepository.UpdateResult.Error(
                                errorMessage = errorState.message,
                                code = errorState.status
                            )
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.e(e, "Network error while Update Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.Error("Network error: ${e.message}", ex = e)
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error while Update Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.Error("An unexpected error occurred: ${e.message}", ex = e)
            }
        }
    }
}
