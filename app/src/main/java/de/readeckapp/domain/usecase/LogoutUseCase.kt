package de.readeckapp.domain.usecase

import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.datetime.Instant
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val bookmarkRepository: BookmarkRepository
) {

    suspend fun execute(){
        settingsDataStore.clearCredentials()
        bookmarkRepository.deleteAllBookmarks()
        settingsDataStore.saveLastBookmarkTimestamp(Instant.fromEpochMilliseconds(0))
    }
}