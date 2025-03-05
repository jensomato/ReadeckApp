package de.readeckapp.domain.usecase

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.worker.LoadArticleWorker
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject

class LoadBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val readeckApi: ReadeckApi,
    private val workManager: WorkManager
) {
    suspend fun execute(pageSize: Int, offset: Int, updatedSince: Instant?) {
        Timber.d("loadNextPage(pageSize=$pageSize, offset=$offset, updatedSince=$updatedSince")
        val isFullSync = updatedSince == null
        try {
            val response = readeckApi.getBookmarks(pageSize, offset, updatedSince, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))
            if (response.isSuccessful && response.body() != null) {
                val bookmarks = (response.body() as List<BookmarkDto>).map { it.toDomain() }
                val totalCount = response.headers()["total-count"]?.toInt()!!
                val totalPages = response.headers()["total-pages"]?.toInt()!!
                val currentPage = response.headers()["current-page"]?.toInt()!!
                Timber.d("currentPage=$currentPage")
                Timber.d("totalPages=$totalPages")
                Timber.d("totalCount=$totalCount")
                if (isFullSync) {
                    bookmarkRepository.deleteAllBookmarks()
                }
                bookmarkRepository.insertBookmarks(bookmarks)
                bookmarks.forEach {
                    val request = OneTimeWorkRequestBuilder<LoadArticleWorker>().setInputData(
                        Data.Builder().putString("bookmarkId", it.id).build()
                    ).build()
                    workManager.enqueue(request)
                }
                if (currentPage < totalPages) {
                    val newOffset = offset + pageSize
                    execute(pageSize, newOffset, updatedSince)
                }
            }
        } catch (e: Exception) {
            Timber.e("error work=$e", e)
        }
    }
}
