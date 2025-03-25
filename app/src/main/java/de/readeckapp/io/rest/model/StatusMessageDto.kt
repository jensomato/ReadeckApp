package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class StatusMessageDto(
    val status: Int,
    val message: String,
)
