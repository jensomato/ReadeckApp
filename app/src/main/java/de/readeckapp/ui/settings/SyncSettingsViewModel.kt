package de.readeckapp.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.R
import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.worker.LoadBookmarksWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val _uiState =
        MutableStateFlow(
            SyncSettingsUiState(
                autoSyncEnabled = false,
                autoSyncTimeframe = AutoSyncTimeframe.MANUAL,
                autoSyncTimeframeOptions = getAutoSyncOptionList(AutoSyncTimeframe.MANUAL),
                showDialog = false,
                autoSyncTimeframeLabel = AutoSyncTimeframe.MANUAL.toLabelResource()
            )
        )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val autoSyncEnabled = settingsDataStore.isAutoSyncEnabled()
            val autoSyncTimeframe = settingsDataStore.getAutoSyncTimeframe()
            _uiState.value = SyncSettingsUiState(
                autoSyncEnabled = autoSyncEnabled,
                autoSyncTimeframe = autoSyncTimeframe,
                autoSyncTimeframeOptions = getAutoSyncOptionList(autoSyncTimeframe),
                showDialog = false,
                autoSyncTimeframeLabel = autoSyncTimeframe.toLabelResource()
            )
        }
    }

    fun onClickDoFullSyncNow() {
        viewModelScope.launch {
            try {
                LoadBookmarksWorker.enqueue(
                    context,
                    isInitialLoad = true
                ) // Enqueue for incremental sync
            } catch (e: Exception) {
                // Handle errors (e.g., show error message)
                println("Error loading bookmarks: ${e.message}")
            }
        }
    }

    fun onClickAutoSync() {
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun onDismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    fun onAutoSyncTimeframeSelected(selected: AutoSyncTimeframe) {
        Timber.d("onAutoSyncTimeframeSelected [selected=$selected]")
        _uiState.value = _uiState.value.copy(
            autoSyncTimeframe = selected,
            autoSyncTimeframeOptions = getAutoSyncOptionList(selected),
            autoSyncTimeframeLabel = selected.toLabelResource()
        )
    }

    fun onClickAutoSyncSwitch(value: Boolean) {
        viewModelScope.launch {
            Timber.d("switch $value")
            settingsDataStore.setAutoSyncEnabled(value)
            _uiState.value = _uiState.value.copy(autoSyncEnabled = value)
        }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null }
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    fun getAutoSyncOptionList(selected: AutoSyncTimeframe): List<AutoSyncTimeframeOption> {
        return AutoSyncTimeframe.entries.map {
            AutoSyncTimeframeOption(
                autoSyncTimeframe = it,
                label = it.toLabelResource(),
                selected = it == selected
            )
        }
    }
}

data class SyncSettingsUiState(
    val autoSyncEnabled: Boolean,
    val autoSyncTimeframe: AutoSyncTimeframe,
    val autoSyncTimeframeOptions: List<AutoSyncTimeframeOption>,
    val showDialog: Boolean,
    @StringRes
    val autoSyncTimeframeLabel: Int
)

data class AutoSyncTimeframeOption(
    val autoSyncTimeframe: AutoSyncTimeframe,
    @StringRes
    val label: Int,
    val selected: Boolean
)

@StringRes
fun AutoSyncTimeframe.toLabelResource(): Int {
    return when (this) {
        AutoSyncTimeframe.MANUAL -> R.string.auto_sync_timeframe_manual
        AutoSyncTimeframe.HOURS_01 -> R.string.auto_sync_timeframe_01_hours
        AutoSyncTimeframe.HOURS_06 -> R.string.auto_sync_timeframe_06_hours
        AutoSyncTimeframe.HOURS_12 -> R.string.auto_sync_timeframe_12_hours
        AutoSyncTimeframe.DAYS_01 -> R.string.auto_sync_timeframe_01_days
        AutoSyncTimeframe.DAYS_07 -> R.string.auto_sync_timeframe_07_days
        AutoSyncTimeframe.DAYS_14 -> R.string.auto_sync_timeframe_14_days
        AutoSyncTimeframe.DAYS_30 -> R.string.auto_sync_timeframe_30_days
    }
}
