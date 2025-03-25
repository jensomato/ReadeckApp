package de.readeckapp.domain

import de.readeckapp.domain.model.AuthenticationDetails
import de.readeckapp.domain.model.User
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import de.readeckapp.io.rest.model.AuthenticationResponseDto
import de.readeckapp.io.rest.model.StatusMessageDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var readeckApi: ReadeckApi
    private lateinit var json: Json
    private lateinit var userRepository: UserRepositoryImpl

    // MutableStateFlows for testing
    private lateinit var urlFlow: MutableStateFlow<String?>
    private lateinit var usernameFlow: MutableStateFlow<String?>
    private lateinit var passwordFlow: MutableStateFlow<String?>
    private lateinit var tokenFlow: MutableStateFlow<String?>

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined) // Use Unconfined for immediate execution
        settingsDataStore = mockk(relaxed = true) // relaxed = true to avoid specifying every method
        readeckApi = mockk()
        json = Json { ignoreUnknownKeys = true }

        // Initialize MutableStateFlows
        urlFlow = MutableStateFlow(null)
        usernameFlow = MutableStateFlow(null)
        passwordFlow = MutableStateFlow(null)
        tokenFlow = MutableStateFlow(null)

        // Mock SettingsDataStore to return the MutableStateFlows
        every { settingsDataStore.urlFlow } returns urlFlow
        every { settingsDataStore.usernameFlow } returns usernameFlow
        every { settingsDataStore.passwordFlow } returns passwordFlow
        every { settingsDataStore.tokenFlow } returns tokenFlow

        userRepository = UserRepositoryImpl(settingsDataStore, readeckApi, json)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeAuthenticationDetails returns AuthenticationDetails when all data is available`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val token = "testtoken"

        // Emit values to the MutableStateFlows
        urlFlow.value = url
        usernameFlow.value = username
        passwordFlow.value = password
        tokenFlow.value = token

        // Act
        val result = userRepository.observeAuthenticationDetails().first()

        // Assert
        assertEquals(AuthenticationDetails(url, username, password, token), result)
    }

    @Test
    fun `observeAuthenticationDetails returns null when any data is missing`() = runTest {
        // Arrange
        urlFlow.value = "https://example.com"
        usernameFlow.value = "testuser"
        passwordFlow.value = null // Missing password
        tokenFlow.value = "testtoken"

        // Act
        val result = userRepository.observeAuthenticationDetails().first()

        // Assert
        assertNull(result)
    }

    @Test
    fun `login with username and password returns Success on successful response`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val token = "testtoken"
        val authenticationResponseDto = AuthenticationResponseDto("id", token)

        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } returns Response.success(authenticationResponseDto)

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assertEquals(UserRepository.LoginResult.Success, result)
        coVerify { settingsDataStore.saveCredentials(url, username, password, token) }
    }

    @Test
    fun `login with username and password returns Error on unsuccessful response with error body`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val errorMessage = "Invalid credentials"
        val errorCode = 401
        val errorBody = StatusMessageDto(errorCode, errorMessage)
        val errorResponse = Response.error<AuthenticationResponseDto>(errorCode, json.encodeToString(StatusMessageDto.serializer(), errorBody).toResponseBody(null))

        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } returns errorResponse

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assertEquals(UserRepository.LoginResult.Error(errorMessage, errorCode), result)
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `login with username and password returns Error on unsuccessful response with empty error body`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val errorCode = 401
        val errorResponse = Response.error<AuthenticationResponseDto>(errorCode, "".toResponseBody(null))

        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } returns errorResponse

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assertEquals(UserRepository.LoginResult.Error("Empty error body", errorCode), result)
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `login with username and password returns Error on unsuccessful response with malformed error body`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val errorCode = 401
        val errorResponse = Response.error<AuthenticationResponseDto>(errorCode, "<html><body><h1>Error</h1></body></html>".toResponseBody(null))

        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } returns errorResponse

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assert(result is UserRepository.LoginResult.Error)
        assert((result as UserRepository.LoginResult.Error).errorMessage.startsWith("Failed to parse error"))
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `login with username and password returns Error on IOException`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val errorMessage = "Network error"
        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } throws IOException(errorMessage)

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assert(result is UserRepository.LoginResult.Error)
        assert((result as UserRepository.LoginResult.Error).errorMessage.startsWith("Network error"))
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `login with username and password returns Error on generic Exception`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val errorMessage = "Generic error"
        coEvery { readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app")) } throws Exception(errorMessage)

        // Act
        val result = userRepository.login(url, username, password)

        // Assert
        assert(result is UserRepository.LoginResult.Error)
        assert((result as UserRepository.LoginResult.Error).errorMessage.startsWith("An unexpected error occurred"))
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `observeIsLoggedIn returns true when AuthenticationDetails is not null`() = runTest {
        // Arrange
        val url = "https://example.com"
        val username = "testuser"
        val password = "testpassword"
        val token = "testtoken"

        // Emit values to the MutableStateFlows
        urlFlow.value = url
        usernameFlow.value = username
        passwordFlow.value = password
        tokenFlow.value = token

        // Act
        val result = userRepository.observeIsLoggedIn().first()

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `observeIsLoggedIn returns false when AuthenticationDetails is null`() = runTest {
        // Arrange
        urlFlow.value = "https://example.com"
        usernameFlow.value = "testuser"
        passwordFlow.value = null
        tokenFlow.value = "testtoken"

        // Act
        val result = userRepository.observeIsLoggedIn().first()

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `observeUser returns User when AuthenticationDetails is not null`() = runTest {
        // Arrange
        val username = "testuser"
        val url = "https://example.com"
        val password = "testpassword"
        val token = "testtoken"

        // Emit values to the MutableStateFlows
        urlFlow.value = url
        usernameFlow.value = username
        passwordFlow.value = password
        tokenFlow.value = token

        // Act
        val result = userRepository.observeUser().first()

        // Assert
        assertEquals(User(username), result)
    }

    @Test
    fun `observeUser returns null when AuthenticationDetails is null`() = runTest {
        // Arrange
        urlFlow.value = "https://example.com"
        usernameFlow.value = "testuser"
        passwordFlow.value = null
        tokenFlow.value = "testtoken"

        // Act
        val result = userRepository.observeUser().first()

        // Assert
        assertNull(result)
    }
}
