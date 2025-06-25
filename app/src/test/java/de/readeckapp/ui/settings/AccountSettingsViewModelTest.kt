package de.readeckapp.ui.settings

import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.LogoutUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var authenticateUseCase: AuthenticateUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: AccountSettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsDataStore = mockk()
        authenticateUseCase = mockk()
        logoutUseCase = mockk()
        coEvery { settingsDataStore.urlFlow } returns MutableStateFlow("")
        coEvery { settingsDataStore.usernameFlow } returns MutableStateFlow("")
        coEvery { settingsDataStore.passwordFlow } returns MutableStateFlow("")
        viewModel = AccountSettingsViewModel(settingsDataStore, logoutUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState should reflect data store values`() = runTest {
        coEvery { settingsDataStore.urlFlow } returns MutableStateFlow("https://example.com")
        coEvery { settingsDataStore.usernameFlow } returns MutableStateFlow("testUser")
        coEvery { settingsDataStore.passwordFlow } returns MutableStateFlow("testPassword")
        viewModel = AccountSettingsViewModel(settingsDataStore, logoutUseCase)

        val uiStateList = viewModel.uiState.take(2).toList()
        assertInitialUiState(uiStateList[0])
        val uiState = uiStateList[1]
        assertEquals("https://example.com", uiState.url)
        assertEquals("testUser", uiState.username)
        assertEquals("testPassword", uiState.password)
        assertFalse(uiState.allowUnencryptedConnection)
    }

    private fun assertInitialUiState(settingsUiState: AccountSettingsUiState) {
        assertEquals("", settingsUiState.url)
        assertEquals("", settingsUiState.username)
        assertEquals("", settingsUiState.password)
        assertFalse(settingsUiState.useApiToken)
        assertFalse(settingsUiState.allowUnencryptedConnection)
    }

    @Test
    fun `onNavigationEventConsumed should reset navigation event`() = runTest {
        viewModel.onClickBack()
        viewModel.onNavigationEventConsumed()
        assertNull(viewModel.navigationEvent.first())
    }

    @Test
    fun `onClickBack should set NavigateBack navigation event`() = runTest {
        viewModel.onClickBack()
        assertEquals(AccountSettingsViewModel.NavigationEvent.NavigateBack, viewModel.navigationEvent.first())
    }
}
