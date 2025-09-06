package de.readeckapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SharedText(
    val url: String,
    val title: String?
)