package de.readeckapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.runtime.mutableStateOf
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val authenticateUseCase: AuthenticateUseCase
) : ViewModel() {
    val bookmarks = mutableStateOf<List<Bookmark>>(emptyList())
    private val _uiState = MutableStateFlow(AccountSettingsUiState("", "", ""))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AccountSettingsUiState(
                settingsDataStore.urlFlow.value ?: "",
                settingsDataStore.usernameFlow.value ?: "",
                "pass"
            )

        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val success = authenticateUseCase.execute(
                _uiState.value.url,
                _uiState.value.username,
                _uiState.value.password
            )
            Timber.d("success=$success")
        }
    }

    fun onUrlChanged(value: String) {
        _uiState.update { it.copy(url = value) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }


}

data class AccountSettingsUiState(
    val url: String,
    val username: String,
    val password: String
)
