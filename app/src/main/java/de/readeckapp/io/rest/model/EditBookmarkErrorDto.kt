package de.readeckapp.io.rest.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class EditBookmarkErrorDto(
    val errors: List<String>? = null,
    val fields: Map<String, FieldError>? = null,
    @SerialName("is_valid")
    val isValid: Boolean? = null
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class FieldError(
        val errors: List<String>? = null,
        @SerialName("is_bound")
        val isBound: Boolean? = null,
        @SerialName("is_null")
        val isNull: Boolean? = null
    )
}
