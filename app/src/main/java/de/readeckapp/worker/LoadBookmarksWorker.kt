package de.readeckapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.usecase.LoadBookmarksUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import timber.log.Timber

@HiltWorker
class LoadBookmarksWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val loadBookmarksUseCase: LoadBookmarksUseCase,
    private val bookmarkRepository: BookmarkRepository,
    private val settingsDataStore: SettingsDataStore,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val isInitialLoad = inputData.getBoolean(PARAM_IS_INITIAL_LOAD, false)

        if (isInitialLoad && isAnotherWorkerRunning()) {
            Timber.i("Another LoadBookmarksWorker is running, exiting early.")
            return Result.success() // Or Result.failure() if you want to signal an error
        }

        if (isInitialLoad) {
            try {
                Timber.i("Performing initial sync: Deleting all bookmarks.")
                bookmarkRepository.deleteAllBookmarks()
                Timber.i("Performing initial sync: Reset lastBookmarkTimestamp.")
                settingsDataStore.saveLastBookmarkTimestamp(Instant.fromEpochMilliseconds(0))
            } catch (e: Exception) {
                Timber.e(e, "Error preparing for initial loading.")
                return Result.failure()
            }
        }

        return when (val result = loadBookmarksUseCase.execute()) {
            is LoadBookmarksUseCase.UseCaseResult.Success -> {
                if(!isAnotherWorkerRunning()) _isRunning.value = false
                Result.success()
            }
            is LoadBookmarksUseCase.UseCaseResult.Error -> {
                Timber.e(result.exception, "Error loading bookmarks")
                if(!isAnotherWorkerRunning()) _isRunning.value = false
                Result.failure() // Or Result.retry() depending on the error
            }
        }
    }

    suspend fun isAnotherWorkerRunning(): Boolean {
        return withContext(Dispatchers.IO) {
            val workInfos = WorkManager.getInstance(applicationContext)
                .getWorkInfosForUniqueWork(UNIQUE_WORK_NAME)
                .get()

            // Check if there's another worker running with a different ID.
            // This prevents the worker from exiting early if it's briefly interrupted
            // and then rescheduled.
            workInfos.any { it.id != id && it.state == WorkInfo.State.RUNNING }
        }
    }

    companion object {
        const val PARAM_IS_INITIAL_LOAD = "isInitialLoad"
        const val UNIQUE_WORK_NAME = "LoadBookmarksSync"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun enqueue(context: Context, isInitialLoad: Boolean = false) {
            val data = Data.Builder().putBoolean(PARAM_IS_INITIAL_LOAD, isInitialLoad).build()

            val request = OneTimeWorkRequestBuilder<LoadBookmarksWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)

            _isRunning.value = true
        }
    }
}
