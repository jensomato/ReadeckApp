package de.readeckapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.domain.mapper.toEntity
import de.readeckapp.io.rest.ReadeckApi
import kotlinx.datetime.Instant
import timber.log.Timber

@HiltWorker
class LoadBookmarksWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val readeckApi: ReadeckApi,
    private val repository: BookmarkRepository,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // TODO: Implement the actual work to load bookmarks
        loadNextPage(10, 0, null)
//        try {
//            val response = readeckApi.getBookmarks(10, 0, null)
//            if (response.isSuccessful) {
//                val totalCount = response.headers()["total-count"]?.toInt()
//                val totalPages = response.headers()["total-pages"]?.toInt()
//                val currentPage = response.headers()["current-page"]?.toInt()
//                Timber.d("currentPage=$currentPage")
//                Timber.d("totalPages=$totalPages")
//                Timber.d("totalCount=$totalCount")
//                val bookmarks = response.body()!!.map { it.toDomain() }
//                repository.insertBookmarks(bookmarks)
//                bookmarks.forEach {
//                    val request = OneTimeWorkRequestBuilder<LoadArticleWorker>().setInputData(
//                        Data.Builder().putString("bookmarkId", it.id).build()
//                    ).build()
//                    workManager.enqueue(request)
//                }
//            }
//            Timber.d("response=$response")
            return Result.success()
//        } catch (e: Exception) {
//            Timber.e("error work=$e", e)
//            // Handle errors (e.g., retry, failure)
//            return Result.failure()
//        }
    }

    suspend fun loadNextPage(pageSize: Int, offset: Int, updatedSince: Instant?) {
        Timber.d("loadNextPage(pageSize=$pageSize, offset=$offset, updatedSince=$updatedSince")
        try {
            val response = readeckApi.getBookmarks(pageSize, offset, updatedSince, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))
            if (response.isSuccessful) {
                val totalCount = response.headers()["total-count"]?.toInt()!!
                val totalPages = response.headers()["total-pages"]?.toInt()!!
                val currentPage = response.headers()["current-page"]?.toInt()!!
                Timber.d("currentPage=$currentPage")
                Timber.d("totalPages=$totalPages")
                Timber.d("totalCount=$totalCount")
                val bookmarks = response.body()!!.map { it.toDomain() }
                repository.insertBookmarks(bookmarks)
                bookmarks.forEach {
                    val request = OneTimeWorkRequestBuilder<LoadArticleWorker>().setInputData(
                        Data.Builder().putString("bookmarkId", it.id).build()
                    ).build()
                    workManager.enqueue(request)
                }
                if (currentPage < totalPages) {
                    val newOffset = offset + pageSize
                    loadNextPage(pageSize, newOffset, updatedSince)
                }
            }
        } catch (e: Exception) {
            Timber.e("error work=$e", e)

        }
    }
}
