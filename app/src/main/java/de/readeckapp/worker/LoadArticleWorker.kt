package de.readeckapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.usecase.LoadArticleUseCase
import de.readeckapp.io.rest.ReadeckApi
import timber.log.Timber

@HiltWorker
class LoadArticleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    val loadArticleUseCase: LoadArticleUseCase
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // TODO: Implement the actual work to load bookmarks
        try {
            Timber.d("Start Work with params=$workerParams")
            val bookmarkId = workerParams.inputData.getString("bookmarkId")
            Timber.d("bookmarkId=$bookmarkId")
            return if (bookmarkId != null) {
                loadArticleUseCase.execute(bookmarkId)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in worker")
            // Handle errors (e.g., retry, failure)
            return Result.failure()
        }
    }
}
