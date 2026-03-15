package de.readeckapp.domain.model

data class AuthenticationDetails(
    val url: String,
    val username: String,
    val token: String
)
