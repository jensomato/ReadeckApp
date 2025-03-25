package de.readeckapp.ui.settings

import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var authenticateUseCase: AuthenticateUseCase
    private lateinit var viewModel: AccountSettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsDataStore = mockk()
        authenticateUseCase = mockk()
        coEvery { settingsDataStore.urlFlow } returns MutableStateFlow("")
        coEvery { settingsDataStore.usernameFlow } returns MutableStateFlow("")
        coEvery { settingsDataStore.passwordFlow } returns MutableStateFlow("")
        viewModel = AccountSettingsViewModel(settingsDataStore, authenticateUseCase)
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
        viewModel = AccountSettingsViewModel(settingsDataStore, authenticateUseCase)

        val uiStateList = viewModel.uiState.take(2).toList()
        assertInitialUiState(uiStateList[0])
        val uiState = uiStateList[1]
        assertEquals("https://example.com", uiState.url)
        assertEquals("testUser", uiState.username)
        assertEquals("testPassword", uiState.password)
    }

    private fun assertInitialUiState(settingsUiState: AccountSettingsUiState) {
        assertEquals("", settingsUiState.url)
        assertEquals("", settingsUiState.username)
        assertEquals("", settingsUiState.password)
        assertNull(settingsUiState.urlError)
        assertNull(settingsUiState.usernameError)
        assertNull(settingsUiState.passwordError)
        assertNull(settingsUiState.authenticationResult)
        assertFalse(settingsUiState.loginEnabled)
    }

    @Test
    fun `onUrlChanged should update url in uiState`() = runTest {
        viewModel.onUrlChanged("https://newurl.com")
        val uiState = viewModel.uiState.first()
        assertEquals("https://newurl.com", uiState.url)
    }

    @Test
    fun `onUsernameChanged should update username in uiState`() = runTest {
        viewModel.onUsernameChanged("newUsername")
        val uiState = viewModel.uiState.first()
        assertEquals("newUsername", uiState.username)
    }

    @Test
    fun `onPasswordChanged should update password in uiState`() = runTest {
        viewModel.onPasswordChanged("newPassword")
        val uiState = viewModel.uiState.first()
        assertEquals("newPassword", uiState.password)
    }

    @Test
    fun `onUrlChanged with valid URL should not set urlError`() = runTest {
        viewModel.onUrlChanged("https://validurl.com")
        val uiState = viewModel.uiState.first()
        assertNull(uiState.urlError)
    }

    @Test
    fun `onUrlChanged with invalid URL should set urlError`() = runTest {
        viewModel.onUrlChanged("invalid-url")
        val uiState = viewModel.uiState.first()
        assertEquals(R.string.account_settings_url_error, uiState.urlError)
    }

    @Test
    fun `onUsernameChanged with blank username should set usernameError`() = runTest {
        viewModel.onUsernameChanged("")
        val uiState = viewModel.uiState.first()
        assertEquals(R.string.account_settings_username_error, uiState.usernameError)
    }

    @Test
    fun `onPasswordChanged with blank password should set passwordError`() = runTest {
        viewModel.onPasswordChanged("")
        val uiState = viewModel.uiState.first()
        assertEquals(R.string.account_settings_password_error, uiState.passwordError)
    }

    @Test
    fun `login should call authenticateUseCase with correct parameters`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        viewModel.onUrlChanged("https://example.com/api")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        coVerify { authenticateUseCase.execute("https://example.com/api", "testUser", "testPassword") }
    }

    @Test
    fun `login should add api suffix to url and call authenticateUseCase with correct parameters`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        viewModel.onUrlChanged("https://example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        coVerify { authenticateUseCase.execute("https://example.com/api", "testUser", "testPassword") }
    }

    @Test
    fun `login should update authenticationResult on success`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        viewModel.onUrlChanged("https://example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertEquals(AuthenticationResult.Success, uiState.authenticationResult)
    }

    @Test
    fun `login should update authenticationResult on authentication failure`() = runTest {
        val errorMessage = "Authentication failed"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.AuthenticationFailed(errorMessage)
        viewModel.onUrlChanged("https://example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertEquals(AuthenticationResult.AuthenticationFailed(errorMessage), uiState.authenticationResult)
    }

    @Test
    fun `login should update authenticationResult on network error`() = runTest {
        val errorMessage = "Network error"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.NetworkError(errorMessage)
        viewModel.onUrlChanged("https://example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertEquals(AuthenticationResult.NetworkError(errorMessage), uiState.authenticationResult)
    }

    @Test
    fun `login should update authenticationResult on generic error`() = runTest {
        val errorMessage = "Generic error"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.GenericError(errorMessage)
        viewModel.onUrlChanged("https://example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPassword")
        viewModel.login()
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertEquals(AuthenticationResult.GenericError(errorMessage), uiState.authenticationResult)
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

    @Test
    fun `loginEnabled should be false when url is invalid`() = runTest {
        viewModel.onUrlChanged("invalid-url")
        viewModel.onUsernameChanged("test")
        viewModel.onPasswordChanged("test")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `loginEnabled should be false when username is blank`() = runTest {
        viewModel.onUrlChanged("https://validurl.com")
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("test")
        assertFalse(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `loginEnabled should be false when password is blank`() = runTest {
        viewModel.onUrlChanged("https://validurl.com")
        viewModel.onUsernameChanged("test")
        viewModel.onPasswordChanged("")
        assertFalse(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `loginEnabled should be true when url, username and password are valid`() = runTest {
        viewModel.onUrlChanged("https://validurl.com")
        viewModel.onUsernameChanged("test")
        viewModel.onPasswordChanged("test")
        assertTrue(viewModel.uiState.first().loginEnabled)
    }
}
