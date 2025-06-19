package de.readeckapp.ui.login

import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
        assert(uiState.passwordOrApiToken.isEmpty())
        assert(uiState.urlError == null)
        assert(uiState.usernameError == null)
        assert(uiState.passwordOrApiTokenError == null)
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
            assert(urlError == R.string.login_url_empty_error)
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
            assert(urlError == R.string.login_url_invalid_error)

        }
    }

    @Test
    fun `onUsernameChanged, validate and update state (empty username)`() = runTest {
        val testUsername = ""
        viewModel.onUsernameChanged(testUsername)

        with(viewModel.uiState.value){
            assert(username == testUsername)
            assert(usernameError == R.string.login_username_empty_error)
        }
    }

    @Test
    fun `onUsernameChanged, validate and update state (valid username)`() = runTest {
        val testUsername = "test"
        viewModel.onUsernameChanged(testUsername)

        with(viewModel.uiState.value) {
            assert(username == testUsername)
            assert(usernameError == null)
        }
    }

    @Test
    fun `onPasswordOrApiTokenChanged, validate and update state (empty password)`() = runTest {
        val testPassword = ""

        if(viewModel.uiState.value.useApiToken) {
            viewModel.onToggleUseApiToken()
        }

        viewModel.onPasswordOrApiTokenChanged(testPassword)

        with(viewModel.uiState.value) {
            assert(passwordOrApiToken == testPassword)
            assert(passwordOrApiTokenError == R.string.login_password_empty_error)
        }
    }

    @Test
    fun `onPasswordOrApiTokenChanged, validate and update state (empty API token)`() = runTest {
        val testApiToken = ""

        if(!viewModel.uiState.value.useApiToken) {
            viewModel.onToggleUseApiToken()
        }

        viewModel.onPasswordOrApiTokenChanged(testApiToken)

        with(viewModel.uiState.value) {
            assert(passwordOrApiToken == testApiToken)
            assert(passwordOrApiTokenError == R.string.login_apitoken_empty_error)
        }
    }

    @Test
    fun `onPasswordOrApiTokenChanged, validate and update state (valid password or api token)`() = runTest {
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        viewModel.onPasswordOrApiTokenChanged(testPasswordOrApiToken)

        with(viewModel.uiState.value) {
            assert(passwordOrApiToken == testPasswordOrApiToken)
            assert(passwordOrApiTokenError == null)
        }
    }

    @Test
    fun `onToggleShowPasswordOrApiToken, update state`() = runTest {
        val initialValue = viewModel.uiState.value.showPassword

        viewModel.onToggleShowPasswordOrApiToken()

        assert(viewModel.uiState.value.showPassword == !initialValue)
    }

    @Test
    fun `onToggleUseUnencryptedConnection, update state`() = runTest {
        val initialValue = viewModel.uiState.value.useUnencryptedConnection

        viewModel.onToggleUseUnencryptedConnection()

        assert(viewModel.uiState.value.useUnencryptedConnection == !initialValue)
    }

    @Test
    fun `onToggleUseApiToken, update state`() = runTest {
        val initialValue = viewModel.uiState.value.useApiToken

        viewModel.onToggleUseApiToken()

        with(viewModel.uiState.value) {
            assert(useApiToken == !initialValue)
            assert(showPassword == !initialValue)
            assert(username == "")
            assert(usernameError == null)
            assert(passwordOrApiToken == "")
            assert(passwordOrApiTokenError == null)
        }
    }

    @Test
    fun `onAuthenticationResultConsumed, update state`() = runTest {
        viewModel.onAuthenticationResultConsumed()

        assert(viewModel.uiState.value.authenticationResult == null)
    }

    @Test
    fun `onClickLogin should call authenticateUseCase with correct parameters`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        val testUrlWithApiSuffix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"
        val urlPrefix = if(viewModel.uiState.value.useUnencryptedConnection) {
            "http://"
        } else "https://"

        with(viewModel) {
            onUrlChanged(testUrlWithApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        coVerify { authenticateUseCase.execute(urlPrefix + testUrlWithApiSuffix, testUsername, testPasswordOrApiToken) }
    }

    @Test
    fun `onClickLogin should add api suffix to URL and call authenticateUseCase with correct parameters`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        val testUrlWithoutApiSuffix = "example.com"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"
        val urlPrefix = if(viewModel.uiState.value.useUnencryptedConnection) {
            "http://"
        } else "https://"

        with(viewModel) {
            onUrlChanged(testUrlWithoutApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        coVerify { authenticateUseCase.execute("$urlPrefix${testUrlWithoutApiSuffix}/api", testUsername, testPasswordOrApiToken) }
    }

    @Test
    fun `onClickLogin should remove url schema if present and call authenticateUseCase with correct parameters`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success

        val urlPrefix = if(viewModel.uiState.value.useUnencryptedConnection) {
            "http://"
        } else "https://"

        val testUrlWithSchemaPrefix = "${urlPrefix}example.com/api"
        val testUrlWithoutSchemaPrefix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        with(viewModel) {
            onUrlChanged(testUrlWithSchemaPrefix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        assert(viewModel.uiState.value.url == testUrlWithoutSchemaPrefix)

        advanceUntilIdle()

        coVerify { authenticateUseCase.execute(urlPrefix + testUrlWithoutSchemaPrefix, testUsername, testPasswordOrApiToken) }
    }

    @Test
    fun `onClickLogin should update authenticationResult on success`() = runTest {
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.Success
        val testUrlWithApiSuffix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        with(viewModel) {
            onUrlChanged(testUrlWithApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        assert(viewModel.uiState.value.authenticationResult == AuthenticationResult.Success)
    }

    @Test
    fun `onClickLogin should update authenticationResult on authentication failure`() = runTest {
        val errorMessage = "Authentication Failure"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.AuthenticationFailed(errorMessage)
        val testUrlWithApiSuffix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        with(viewModel) {
            onUrlChanged(testUrlWithApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        assert(viewModel.uiState.value.authenticationResult == AuthenticationResult.AuthenticationFailed(errorMessage))
    }

    @Test
    fun `onClickLogin should update authenticationResult on network error`() = runTest {
        val errorMessage = "Network error"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.NetworkError(errorMessage)
        val testUrlWithApiSuffix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        with(viewModel) {
            onUrlChanged(testUrlWithApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        assert(viewModel.uiState.value.authenticationResult == AuthenticationResult.NetworkError(errorMessage))
    }

    @Test
    fun `onClickLogin should update authenticationResult on generic error`() = runTest {
        val errorMessage = "Generic error"
        coEvery { authenticateUseCase.execute(any(), any(), any()) } returns AuthenticationResult.GenericError(errorMessage)
        val testUrlWithApiSuffix = "example.com/api"
        val testUsername = "testUser"
        val testPasswordOrApiToken = "testPasswordOrApiToken"

        with(viewModel) {
            onUrlChanged(testUrlWithApiSuffix)
            onUsernameChanged(testUsername)
            onPasswordOrApiTokenChanged(testPasswordOrApiToken)
            onClickLogin()
        }

        advanceUntilIdle()

        assert(viewModel.uiState.value.authenticationResult == AuthenticationResult.GenericError(errorMessage))
    }
}