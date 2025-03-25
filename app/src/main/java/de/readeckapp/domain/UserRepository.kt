package de.readeckapp.domain

import de.readeckapp.domain.model.AuthenticationDetails
import de.readeckapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeIsLoggedIn(): Flow<Boolean>
    fun observeUser(): Flow<User?>
    fun observeAuthenticationDetails(): Flow<AuthenticationDetails?>
    suspend fun login(url: String, username: String, password: String): LoginResult
    suspend fun login(url: String, appToken: String): LoginResult
    suspend fun logout()
    sealed class LoginResult {
        data object Success: LoginResult()
        data class Error(val errorMessage: String, val code: Int? = null, val ex: Exception? = null): LoginResult()
        data class NetworkError(val errorMessage: String): LoginResult()
    }
}
