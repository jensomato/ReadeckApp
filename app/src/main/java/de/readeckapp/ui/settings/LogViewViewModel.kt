package de.readeckapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.R
import de.readeckapp.util.getLatestLogFile
import de.readeckapp.util.logAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LogViewViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onRefresh()
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    fun onRefresh() {
        Timber.d("refresh")
        viewModelScope.launch {
            _uiState.value = getLatestLogFile()?.let {
                Timber.d("file=$it")
                UiState.Success(
                    logContent = it.readText(),
                    shareIntentUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            it
                        )
                )
            }  ?: UiState.Error(R.string.log_view_no_log_file_found)
        }
    }

    fun onShareLogs() {
        logAppInfo()
        _navigationEvent.update { NavigationEvent.ShowShareDialog }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null }
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object ShowShareDialog : NavigationEvent()
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(
            val logContent: String,
            val shareIntentUri: Uri
        ) : UiState()
        data class Error(@StringRes val message: Int) : UiState()
    }
}
