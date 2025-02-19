package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationResponseDto(
    val id: String,
    val token: String,
)
