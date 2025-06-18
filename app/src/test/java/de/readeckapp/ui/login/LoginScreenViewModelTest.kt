package de.readeckapp.ui.login

import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authenticateUseCase: AuthenticateUseCase
    private lateinit var viewModel: LoginScreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authenticateUseCase = mockk()

        viewModel = LoginScreenViewModel(authenticateUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is correct`() = runTest {
        viewModel = LoginScreenViewModel(authenticateUseCase)
        val uiState = viewModel.uiState.value

        assert(uiState.url.isEmpty())
        assert(uiState.username.isEmpty())
        assert(uiState.password.isEmpty())
        assert(uiState.urlError == null)
        assert(uiState.usernameError == null)
        assert(uiState.passwordError == null)
        assert(uiState.authenticationResult == null)
        assert(uiState.useApiToken == false)
        assert(uiState.useUnencryptedConnection == false)
        assert(uiState.showPassword == false)
        assert(uiState.isLoading == false)
        assert(uiState.loginEnabled == false)
    }

    @Test
    fun `onUrlChanged, validate and update state (empty url)`() = runTest {
        val testUrl = ""
        viewModel.onUrlChanged(testUrl)

        with(viewModel.uiState.value) {
            assert(url == testUrl)
            assert(urlError == R.string.account_settings_url_empty_error)
        }
    }

    @Test
    fun `onUrlChanged, validate and update state (valid url)`() = runTest {
        val testUrl = "example.domain"
        viewModel.onUrlChanged(testUrl)

        with(viewModel.uiState.value){
            assert(url == testUrl)
            assert(urlError == null)
        }
    }

    @Test
    fun `onUrlChanged, validate and update state (invalid url)`() = runTest {
        val testUrl = "invalid()url"
        viewModel.onUrlChanged(testUrl)

        with(viewModel.uiState.value){
            assert(url == testUrl)
            assert(urlError == R.string.account_settings_url_error)

        }
    }
}