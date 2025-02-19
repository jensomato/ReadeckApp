package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationRequestDto(
    val username: String,
    val password: String,
    val application: String
)
