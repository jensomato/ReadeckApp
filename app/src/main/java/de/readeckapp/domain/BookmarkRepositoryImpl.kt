package de.readeckapp.domain

import de.readeckapp.coroutine.IoDispatcher
import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.domain.mapper.toEntity
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.model.BookmarkCounts
import de.readeckapp.domain.model.BookmarkListItem
import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.db.model.BookmarkEntity
import de.readeckapp.io.db.model.RemoteBookmarkIdEntity
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
        favorite: Boolean?,
        state: Bookmark.State?
    ): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByFilters(
            type = type?.let {
                when (it) {
                    Bookmark.Type.Article -> BookmarkEntity.Type.ARTICLE
                    Bookmark.Type.Picture -> BookmarkEntity.Type.PHOTO
                    Bookmark.Type.Video -> BookmarkEntity.Type.VIDEO
                }
            },
            isUnread = unread,
            isArchived = archived,
            isFavorite = favorite,
            state = state?.let {
                when (it) {
                    Bookmark.State.LOADED -> BookmarkEntity.State.LOADED
                    Bookmark.State.ERROR -> BookmarkEntity.State.ERROR
                    Bookmark.State.LOADING -> BookmarkEntity.State.LOADING
                }
            }
        ).map { bookmarks -> bookmarks.map { it.toDomain() } }
    }

    override fun observeBookmarkListItems(
        type: Bookmark.Type?,
        unread: Boolean?,
        archived: Boolean?,
        favorite: Boolean?,
        state: Bookmark.State?
    ): Flow<List<BookmarkListItem>> {
        return bookmarkDao.getBookmarkListItemsByFilters(
            type = type?.let {
                when (it) {
                    Bookmark.Type.Article -> BookmarkEntity.Type.ARTICLE
                    Bookmark.Type.Picture -> BookmarkEntity.Type.PHOTO
                    Bookmark.Type.Video -> BookmarkEntity.Type.VIDEO
                }
            },
            isUnread = unread,
            isArchived = archived,
            isFavorite = favorite,
            state = state?.let {
                when (it) {
                    Bookmark.State.LOADED -> BookmarkEntity.State.LOADED
                    Bookmark.State.ERROR -> BookmarkEntity.State.ERROR
                    Bookmark.State.LOADING -> BookmarkEntity.State.LOADING
                }
            }
        ).map { listItems ->
            listItems.map { listItem ->
                BookmarkListItem(
                    id = listItem.id,
                    url = listItem.url,
                    title = listItem.title,
                    siteName = listItem.siteName,
                    isMarked = listItem.isMarked,
                    isArchived = listItem.isArchived,
                    isRead = listItem.readProgress == 100,
                    thumbnailSrc = listItem.thumbnailSrc,
                    iconSrc = listItem.iconSrc,
                    imageSrc = listItem.imageSrc,
                    labels = listItem.labels,
                    type = when (listItem.type) {
                        BookmarkEntity.Type.ARTICLE -> Bookmark.Type.Article
                        BookmarkEntity.Type.PHOTO -> Bookmark.Type.Picture
                        BookmarkEntity.Type.VIDEO -> Bookmark.Type.Video
                    }
                )
            }
        }
    }

    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {
        bookmarkDao.insertBookmarksWithArticleContent(bookmarks.map { it.toEntity() })
    }

    override suspend fun getBookmarkById(id: String): Bookmark {
        return bookmarkDao.getBookmarkById(id).toDomain()
    }

    override fun observeBookmark(id: String): Flow<Bookmark?> {
        return bookmarkDao.observeBookmarkWithArticleContent(id).map {
            it?.let {
                it.bookmark.toDomain().copy(articleContent = it.articleContent?.content)
            }
        }
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

    override suspend fun updateBookmark(
        bookmarkId: String,
        isFavorite: Boolean?,
        isArchived: Boolean?,
        isRead: Boolean?
    ): BookmarkRepository.UpdateResult {
        return withContext(dispatcher) {
            try {
                val response =
                    readeckApi.editBookmark(
                        id = bookmarkId,
                        body = EditBookmarkDto(
                            isMarked = isFavorite,
                            isArchived = isArchived,
                            readProgress = isRead?.let { if (it) 100 else 0 }
                        )
                    )
                if (response.isSuccessful) {
                    Timber.i("Update Bookmark successful")
                    BookmarkRepository.UpdateResult.Success
                } else {
                    val code = response.code()
                    val errorBodyString = response.errorBody()?.string()
                    Timber.w("Error while Update Bookmark [code=$code, body=$errorBodyString]")
                    when (code) {
                        422 -> {
                            if (!errorBodyString.isNullOrBlank()) {
                                try {
                                    json.decodeFromString<EditBookmarkErrorDto>(errorBodyString)
                                        .let {
                                            BookmarkRepository.UpdateResult.Error(
                                                it.errors.toString(),
                                                response.code()
                                            )
                                        }
                                } catch (e: SerializationException) {
                                    Timber.e(e, "Failed to parse error: ${e.message}")
                                    BookmarkRepository.UpdateResult.Error(
                                        errorMessage = "Failed to parse error: ${e.message}",
                                        code = response.code(),
                                        ex = e
                                    )
                                }
                            } else {
                                Timber.e("Empty error body")
                                BookmarkRepository.UpdateResult.Error(
                                    errorMessage = "Empty error body",
                                    code = code
                                )
                            }
                        }

                        else -> {
                            val errorState = handleStatusMessage(response.code(), errorBodyString)
                            BookmarkRepository.UpdateResult.Error(
                                errorMessage = errorState.message,
                                code = errorState.status
                            )
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.e(e, "Network error while Update Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.NetworkError("Network error: ${e.message}", ex = e)
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error while Update Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.Error(
                    "An unexpected error occurred: ${e.message}",
                    ex = e
                )
            }
        }
    }

    override suspend fun deleteBookmark(id: String): BookmarkRepository.UpdateResult {
        return withContext(dispatcher) {
            try {
                val response =
                    readeckApi.deleteBookmark(id = id)
                if (response.isSuccessful) {
                    bookmarkDao.deleteBookmark(id)
                    Timber.i("Delete Bookmark successful")
                    BookmarkRepository.UpdateResult.Success
                } else {
                    val code = response.code()
                    val errorBodyString = response.errorBody()?.string()
                    Timber.w("Error while Delete Bookmark [code=$code, body=$errorBodyString]")
                    val errorState = handleStatusMessage(code, errorBodyString)
                    BookmarkRepository.UpdateResult.Error(
                        errorMessage = errorState.message,
                        code = errorState.status
                    )
                }
            } catch (e: IOException) {
                Timber.e(e, "Network error while Delete Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.NetworkError("Network error: ${e.message}", ex = e)
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error while Delete Bookmark: ${e.message}")
                BookmarkRepository.UpdateResult.Error(
                    "An unexpected error occurred: ${e.message}",
                    ex = e
                )
            }
        }
    }

    private fun handleStatusMessage(code: Int, errorBody: String?): StatusMessageDto {
        return if (!errorBody.isNullOrBlank()) {
            try {
                json.decodeFromString<StatusMessageDto>(errorBody)
            } catch (e: SerializationException) {
                Timber.e(e, "Failed to parse error: ${e.message}")
                StatusMessageDto(
                    status = code,
                    message = "Failed to parse error: ${e.message}"
                )
            }
        } else {
            Timber.e("Empty error body")
            StatusMessageDto(
                status = code,
                message = "Empty error body"
            )
        }
    }

    override suspend fun performFullSync(): BookmarkRepository.SyncResult = withContext(dispatcher) {
        try {
            bookmarkDao.clearRemoteBookmarkIds() // Clear any previous sync data

            val pageSize = 50
            var offset = 0
            var hasMore = true

            while (hasMore) {
                val response = readeckApi.getBookmarks(limit = pageSize, offset = offset, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))

                if (response.isSuccessful) {
                    val remoteBookmarks = response.body() ?: emptyList()
                    Timber.d("Fetched ${remoteBookmarks.size} remote bookmarks (offset=$offset)")

                    val totalCountHeader = response.headers()[ReadeckApi.Header.TOTAL_COUNT]
                    val totalPagesHeader = response.headers()[ReadeckApi.Header.TOTAL_PAGES]
                    val currentPageHeader = response.headers()[ReadeckApi.Header.CURRENT_PAGE]

                    if (totalCountHeader == null || totalPagesHeader == null || currentPageHeader == null) {
                        return@withContext BookmarkRepository.SyncResult.Error("Missing headers in API response")
                    }

                    val totalCount = totalCountHeader.toInt()
                    val totalPages = totalPagesHeader.toInt()
                    val currentPage = currentPageHeader.toInt()

                    Timber.d("currentPage=$currentPage")
                    Timber.d("totalPages=$totalPages")
                    Timber.d("totalCount=$totalCount")

                    // Save remote bookmark IDs to the temporary table
                    val remoteBookmarkIdEntities = remoteBookmarks.map { RemoteBookmarkIdEntity(it.id) }
                    bookmarkDao.insertRemoteBookmarkIds(remoteBookmarkIdEntities)

                    if (currentPage < totalPages) {
                        offset += pageSize
                    } else {
                        hasMore = false
                    }
                } else {
                    Timber.e("Full sync failed at offset=$offset with code: ${response.code()}")
                    return@withContext BookmarkRepository.SyncResult.Error(
                        errorMessage = "Full sync failed",
                        code = response.code(),
                        ex = null
                    )
                }
            }

            // After fetching all remote IDs, find local bookmarks to delete
            val deleted = bookmarkDao.removeDeletedBookmars()
            Timber.i("Deleted bookmarks: $deleted")

            bookmarkDao.clearRemoteBookmarkIds() // Clean up the temporary table

            BookmarkRepository.SyncResult.Success(countDeleted = deleted)
        } catch (e: Exception) {
            Timber.e(e, "Full sync failed")
            BookmarkRepository.SyncResult.NetworkError(errorMessage = "Network error during full sync", ex = e)
        }
    }

    override fun observeAllBookmarkCounts(): Flow<BookmarkCounts> {
        return bookmarkDao.observeAllBookmarkCounts().map { entity ->
            if (entity != null) {
                BookmarkCounts(
                    unread = entity.unread,
                    archived = entity.archived,
                    favorite = entity.favorite,
                    article = entity.article,
                    video = entity.video,
                    picture = entity.picture,
                    total = entity.total
                )
            } else {
                BookmarkCounts()
            }
        }
    }
}
