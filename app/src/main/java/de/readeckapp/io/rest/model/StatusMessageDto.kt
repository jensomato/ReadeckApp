package de.readeckapp.io.rest.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StatusMessageDto(
    @SerialName("status")
    val statusField: Int? = null,

    @SerialName("statusCode")
    val statusCodeField: Int? = null,

    @SerialName("message")
    val message: String,

    @Transient
    val status: Int = statusField ?: statusCodeField ?: -1
)
