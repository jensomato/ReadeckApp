package de.readeckapp.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.worker.LoadBookmarksWorker
import javax.inject.Inject

class UpdateBookmarkFavoriteStateUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun execute(bookmarkId: String, isFavorite: Boolean): Result {

        return when(val updateResult = bookmarkRepository.updateBookmark(bookmarkId, isFavorite)) {
            is BookmarkRepository.UpdateResult.Success -> {
                LoadBookmarksWorker.enqueue(context, isInitialLoad = false)
                Result.Success
            }
            is BookmarkRepository.UpdateResult.Error -> Result.GenericError(updateResult.errorMessage)
            is BookmarkRepository.UpdateResult.NetworkError -> Result.NetworkError(updateResult.errorMessage)
        }
    }

    sealed class Result {
        data object Success : Result()
        data class GenericError(val message: String) : Result()
        data class NetworkError(val message: String) : Result()
    }
}
