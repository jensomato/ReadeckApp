package de.readeckapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.model.Theme
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val settingsDataStore: SettingsDataStore
): ViewModel() {
    val theme = settingsDataStore.themeFlow.map {
        it?.let { Theme.valueOf(it) } ?: Theme.SYSTEM
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Theme.SYSTEM
    )
}