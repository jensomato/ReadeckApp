package de.readeckapp.ui.settings

import android.app.Application
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
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

    private lateinit var application: Application
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var authenticateUseCase: AuthenticateUseCase
    private lateinit var viewModel: AccountSettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsDataStore = mockk(relaxed = true)
        authenticateUseCase = mockk(relaxed = true)
        application = mockk(relaxed = true)
        coEvery { settingsDataStore.urlFlow } returns MutableStateFlow("")
        viewModel = AccountSettingsViewModel(application, settingsDataStore, authenticateUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState should reflect data store values`() = runTest {
        coEvery { settingsDataStore.urlFlow } returns MutableStateFlow("https://example.com")
        viewModel = AccountSettingsViewModel(application, settingsDataStore, authenticateUseCase)

        val uiStateList = viewModel.uiState.take(2).toList()
        assertInitialUiState(uiStateList[0])
        val uiState = uiStateList[1]
        assertEquals("https://example.com", uiState.url)
        assertFalse(uiState.allowUnencryptedConnection)
    }

    private fun assertInitialUiState(settingsUiState: AccountSettingsUiState) {
        assertEquals("", settingsUiState.url)
        assertNull(settingsUiState.urlError)
        assertNull(settingsUiState.authenticationResult)
        assertFalse(settingsUiState.loginEnabled)
        assertFalse(settingsUiState.allowUnencryptedConnection)
    }

    @Test
    fun `onUrlChanged should update url in uiState`() = runTest {
        viewModel.onUrlChanged("https://newurl.com")
        val uiState = viewModel.uiState.first()
        assertEquals("https://newurl.com", uiState.url)
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
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `onAllowUnencryptedConnectionChanged should update allowUnencryptedConnection in uiState`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(true)
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.allowUnencryptedConnection)

        viewModel.onAllowUnencryptedConnectionChanged(false)
        val uiState2 = viewModel.uiState.first()
        assertFalse(uiState2.allowUnencryptedConnection)
    }

    @Test
    fun `onUrlChanged with http URL and allowUnencryptedConnection false should set urlError`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(false)
        viewModel.onUrlChanged("http://validurl.com")
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertEquals(R.string.account_settings_url_error, uiState.urlError)
    }

    @Test
    fun `onUrlChanged with https URL and allowUnencryptedConnection false should not set urlError`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(false)
        viewModel.onUrlChanged("https://validurl.com")
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertNull(uiState.urlError)
    }

    @Test
    fun `onUrlChanged with http URL and allowUnencryptedConnection true should not set urlError`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(true)
        viewModel.onUrlChanged("http://validurl.com")
        advanceUntilIdle()
        val uiState = viewModel.uiState.first()
        assertNull(uiState.urlError)
    }

    @Test
    fun `loginEnabled should be true when url is http and allowUnencryptedConnection is true`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(true)
        viewModel.onUrlChanged("http://validurl.com")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `loginEnabled should be false when url is http and allowUnencryptedConnection is false`() = runTest {
        viewModel.onAllowUnencryptedConnectionChanged(false)
        viewModel.onUrlChanged("http://validurl.com")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().loginEnabled)
    }

    @Test
    fun `handleOAuthResult with null intent should reenable login button`() = runTest {
        viewModel.onUrlChanged("https://validurl.com")
        // Start login to disable button
        viewModel.login()
        advanceUntilIdle()
        
        // Simulating returning back without doing anything in the browser
        viewModel.handleOAuthResult(null)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.first().loginEnabled)
    }
}
