package de.readeckapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.UserRepository
import de.readeckapp.domain.model.Theme
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.ui.navigation.BookmarkListRoute
import de.readeckapp.ui.navigation.LoginRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
    userRepository: UserRepository
): ViewModel() {

    val startDestination: StateFlow<Any?> = userRepository.observeIsLoggedIn().map { isLoggedIn ->
        if (isLoggedIn) {
            BookmarkListRoute(sharedUrl = null)
        } else {
            LoginRoute
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val theme = settingsDataStore.themeFlow.map {
        it?.let { Theme.valueOf(it) } ?: Theme.SYSTEM
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Theme.SYSTEM
    )
}