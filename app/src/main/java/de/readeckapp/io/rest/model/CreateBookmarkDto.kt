package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateBookmarkDto(
    val labels: List<String> = emptyList(),
    val title: String,
    val url: String
)
