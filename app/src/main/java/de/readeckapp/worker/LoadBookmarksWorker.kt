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
import de.readeckapp.domain.usecase.LoadBookmarksUseCase
import kotlinx.datetime.Instant
import timber.log.Timber

@HiltWorker
class LoadBookmarksWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val loadBookmarksUseCase: LoadBookmarksUseCase
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        loadBookmarksUseCase.execute(10, 0, null)
            return Result.success()
    }


}
