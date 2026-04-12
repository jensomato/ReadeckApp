package de.readeckapp.domain.usecase

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.mapper.toDomain
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.SyncInfoType
import de.readeckapp.worker.LoadArticleWorker
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

class SyncBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val readeckApi: ReadeckApi,
    private val workManager: WorkManager,
    private val settingsDataStore: SettingsDataStore
) {
    data class SyncBookmarksResult(
        val updated: Int,
        val deleted: Int,
    )

    suspend fun execute(): UseCaseResult<SyncBookmarksResult> {
        val lastSync = settingsDataStore.getLastSyncTimestamp()
            ?: return UseCaseResult.Error(IllegalStateException("No sync timestamp, perform initial load first"))

        try {
            val syncResponse = readeckApi.getBookmarkSyncList(lastSync)
            if (!syncResponse.isSuccessful || syncResponse.body() == null) {
                return UseCaseResult.Error(Exception("Failed to get sync list: ${syncResponse.code()}"))
            }

            val syncList = syncResponse.body()!!
            
            val deletes = syncList.filter { it.type == SyncInfoType.delete }
            val updates = syncList.filter { it.type == SyncInfoType.update }

            // delete local bookmarks
            deletes.forEach { bookmarkRepository.deleteBookmarkLocal(it.id) }

            // 5. Updates laden
            if (updates.isNotEmpty()) {
                var offset = 0
                val pageSize = 50
                var hasMore = true
                while (hasMore) {
                    val response = readeckApi.getBookmarks(pageSize, offset, null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created), updates.map { it.id })
                    if (response.isSuccessful && response.body() != null) {
                        val bookmarks = (response.body() as List<BookmarkDto>).map { it.toDomain() }
                        bookmarkRepository.insertBookmarks(bookmarks)
                        bookmarks.forEach {
                            val request = OneTimeWorkRequestBuilder<LoadArticleWorker>().setInputData(
                                Data.Builder().putString(LoadArticleWorker.PARAM_BOOKMARK_ID, it.id).build()
                            ).build()
                            workManager.enqueue(request)
                        }

                        val totalPages = response.headers()[ReadeckApi.Header.TOTAL_PAGES]?.toInt() ?: 1
                        val currentPage = response.headers()[ReadeckApi.Header.CURRENT_PAGE]?.toInt() ?: 1
                        
                        if (currentPage < totalPages) {
                            offset += pageSize
                        } else {
                            hasMore = false
                        }
                    } else {
                        hasMore = false
                    }
                }
            }

            val lastSyncTime = Clock.System.now()
            settingsDataStore.saveLastSyncTimestamp(lastSyncTime)

            return UseCaseResult.Success(SyncBookmarksResult(updated = updates.size, deleted = deletes.size))
        } catch (e: Exception) {
            Timber.e(e, "Error during sync")
            return UseCaseResult.Error(e)
        }
    }
}
