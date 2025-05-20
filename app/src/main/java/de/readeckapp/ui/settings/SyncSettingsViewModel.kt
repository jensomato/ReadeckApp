package de.readeckapp.ui.settings

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.R
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.domain.usecase.FullSyncUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val settingsDataStore: SettingsDataStore,
    private val fullSyncUseCase: FullSyncUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private var _permissionState: PermissionState? = null
    private val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM, DateFormat.MEDIUM
    )
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val autoSyncEnabled = MutableStateFlow(false)
    private val autoSyncTimeframe = MutableStateFlow(AutoSyncTimeframe.MANUAL)
    private val showDialog = MutableStateFlow<Dialog?>(null)
    private val workInfo: Flow<WorkInfo?> = fullSyncUseCase.workInfoFlow.map { workInfoList ->
        workInfoList.firstOrNull()?.let {
            it
        }
    }

    init {
        viewModelScope.launch {
            autoSyncEnabled.value = settingsDataStore.isAutoSyncEnabled()
            autoSyncTimeframe.value = settingsDataStore.getAutoSyncTimeframe()
        }
    }


    val uiState = combine(autoSyncEnabled, autoSyncTimeframe, showDialog, workInfo, fullSyncUseCase.syncIsRunning) { autoSyncEnabled, autoSyncTimeframe, showDialog, workInfo, syncIsRunning ->
        Timber.d("changed")
        val next = workInfo?.let {
            if (it.state == WorkInfo.State.ENQUEUED) {
                it.nextScheduleTimeMillis
            } else {
                null
            }
        }
        Timber.d("workInfo=$workInfo")

        Timber.d("enabled=$autoSyncEnabled, timeFrame=$autoSyncTimeframe")
        SyncSettingsUiState(
            autoSyncEnabled = autoSyncEnabled,
            autoSyncTimeframe = autoSyncTimeframe,
            autoSyncTimeframeOptions = getAutoSyncOptionList(autoSyncTimeframe),
            showDialog = showDialog,
            autoSyncTimeframeLabel = autoSyncTimeframe.toLabelResource(),
            nextAutoSyncRun = next?.let { dateFormat.format(Date(it)) },
            autoSyncButtonEnabled = syncIsRunning.not()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                SyncSettingsUiState(
                    autoSyncEnabled = false,
                    autoSyncTimeframe = AutoSyncTimeframe.MANUAL,
                    autoSyncTimeframeOptions = getAutoSyncOptionList(AutoSyncTimeframe.MANUAL),
                    showDialog = null,
                    autoSyncTimeframeLabel = AutoSyncTimeframe.MANUAL.toLabelResource(),
                    nextAutoSyncRun = null,
                    autoSyncButtonEnabled = false
                )
        )

    fun onClickDoFullSyncNow() {
        fullSyncUseCase.performFullSync()
    }

    fun onClickAutoSync() {
        showDialog.value = Dialog.AutoSyncTimeframeDialog
    }

    fun onDismissDialog() {
        showDialog.value = null
    }

    fun onAutoSyncTimeframeSelected(selected: AutoSyncTimeframe) {
        Timber.d("onAutoSyncTimeframeSelected [selected=$selected]")
        if (autoSyncEnabled.value) {
            fullSyncUseCase.scheduleFullSyncWorker(selected)
        }
        updateAutoSyncTimeframe(selected)
    }

    fun onClickAutoSyncSwitch(enabled: Boolean) {
        Timber.d("onClickAutoSyncSwitch [enabled=$enabled]")
        if (enabled) {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                    Timber.d("older version, permission is assumed!")
                }
                _permissionState?.status?.isGranted == true -> {
                    Timber.d("permission is already granted")
                }

                _permissionState?.status?.shouldShowRationale == true -> {
                    showDialog.value = Dialog.RationaleDialog
                }
                else ->{
                    showDialog.value = Dialog.PermissionRequest
                }
            }
            fullSyncUseCase.scheduleFullSyncWorker(autoSyncTimeframe.value)
        } else {
            fullSyncUseCase.cancelFullSyncWorker()
        }
        updateAutoSyncEnabled(enabled)
    }

    fun onRationaleDialogConfirm() {
        showDialog.value = Dialog.PermissionRequest
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    private fun getAutoSyncOptionList(selected: AutoSyncTimeframe): List<AutoSyncTimeframeOption> {
        return AutoSyncTimeframe.entries.map {
            AutoSyncTimeframeOption(
                autoSyncTimeframe = it,
                label = it.toLabelResource(),
                selected = it == selected
            )
        }
    }

    private fun updateAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoSyncEnabled(enabled)
            autoSyncEnabled.value = settingsDataStore.isAutoSyncEnabled()
        }
    }

    private fun updateAutoSyncTimeframe(value: AutoSyncTimeframe) {
        viewModelScope.launch {
            settingsDataStore.saveAutoSyncTimeframe(value)
            autoSyncTimeframe.value = settingsDataStore.getAutoSyncTimeframe()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun setPermissionState(permissionState: PermissionState) {
        _permissionState = permissionState
    }
}

@Immutable
data class SyncSettingsUiState(
    val autoSyncEnabled: Boolean,
    val autoSyncTimeframe: AutoSyncTimeframe,
    val autoSyncTimeframeOptions: List<AutoSyncTimeframeOption>,
    val showDialog: Dialog?,
    @StringRes
    val autoSyncTimeframeLabel: Int,
    val nextAutoSyncRun: String?,
    val autoSyncButtonEnabled: Boolean
)

enum class Dialog {
    RationaleDialog,
    AutoSyncTimeframeDialog,
    PermissionRequest
}

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
