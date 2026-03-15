package de.readeckapp.domain

import de.readeckapp.domain.model.AuthenticationDetails
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.StatusMessageDto
import de.readeckapp.io.rest.model.UserProfileDto
import de.readeckapp.io.rest.model.ProviderDto
import de.readeckapp.io.rest.model.UserDto
import de.readeckapp.io.rest.model.SettingsDto
import de.readeckapp.io.rest.model.ReaderSettingsDto
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
import kotlinx.datetime.Clock
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
    private lateinit var authStateFlow: MutableStateFlow<String?>
    private lateinit var tokenFlow: MutableStateFlow<String?>

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        settingsDataStore = mockk(relaxed = true)
        readeckApi = mockk()
        json = Json { ignoreUnknownKeys = true }

        urlFlow = MutableStateFlow(null)
        usernameFlow = MutableStateFlow(null)
        authStateFlow = MutableStateFlow(null)
        tokenFlow = MutableStateFlow(null)

        every { settingsDataStore.urlFlow } returns urlFlow
        every { settingsDataStore.usernameFlow } returns usernameFlow
        every { settingsDataStore.authStateFlow } returns authStateFlow
        every { settingsDataStore.tokenFlow } returns tokenFlow

        userRepository = UserRepositoryImpl(settingsDataStore, readeckApi, json)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeAuthenticationDetails returns AuthenticationDetails when all data is available`() = runTest {
        val url = "https://example.com"
        val username = "testuser"
        val token = "testtoken"

        urlFlow.value = url
        usernameFlow.value = username
        tokenFlow.value = token

        val result = userRepository.observeAuthenticationDetails().first()

        assertEquals(AuthenticationDetails(url, username, token), result)
    }

    @Test
    fun `observeAuthenticationDetails returns null when any data is missing`() = runTest {
        urlFlow.value = "https://example.com"
        usernameFlow.value = "testuser"
        tokenFlow.value = null

        val result = userRepository.observeAuthenticationDetails().first()

        assertNull(result)
    }

    @Test
    fun `login with token returns Success on successful response`() = runTest {
        val url = "https://example.com"
        val token = "testtoken"
        val authState = "{}"
        val username = "testuser"
        
        val userProfileDto = UserProfileDto(
            provider = ProviderDto("test", "test", "test", emptyList(), emptyList()),
            user = UserDto(username, "test@test.com", Clock.System.now(), Clock.System.now(), 
                   SettingsDto(false, "en", ReaderSettingsDto(1, "font", 1, 1, 1, 1)))
        )

        coEvery { readeckApi.userprofile() } returns Response.success(userProfileDto)

        val result = userRepository.login(url, token, authState)

        assertEquals(UserRepository.LoginResult.Success, result)
        coVerify { settingsDataStore.saveCredentials(url, username, token, authState) }
    }

    @Test
    fun `login returns Error on unsuccessful response with error body`() = runTest {
        val url = "https://example.com"
        val token = "testtoken"
        val authState = "{}"
        val errorMessage = "Invalid credentials"
        val errorCode = 401
        val errorBody = StatusMessageDto(errorCode, errorMessage)
        val errorResponse = Response.error<UserProfileDto>(errorCode, json.encodeToString(StatusMessageDto.serializer(), errorBody).toResponseBody(null))

        coEvery { readeckApi.userprofile() } returns errorResponse

        val result = userRepository.login(url, token, authState)

        assertEquals(UserRepository.LoginResult.Error(errorMessage, errorCode), result)
        coVerify { settingsDataStore.clearCredentials() }
    }

    @Test
    fun `login returns Error on IOException`() = runTest {
        val url = "https://example.com"
        val token = "testtoken"
        val authState = "{}"
        val errorMessage = "Network error"
        coEvery { readeckApi.userprofile() } throws IOException(errorMessage)

        val result = userRepository.login(url, token, authState)

        assert(result is UserRepository.LoginResult.Error)
        assert((result as UserRepository.LoginResult.Error).errorMessage.startsWith("Network error"))
        coVerify { settingsDataStore.clearCredentials() }
    }
}
