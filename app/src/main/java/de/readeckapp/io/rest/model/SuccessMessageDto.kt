package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class SuccessMessageDto(
    val status: Int,
    val message: String,
)
