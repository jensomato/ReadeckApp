package de.readeckapp.io.rest.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkSyncDto(
    val id: String,
    val href: String,
    val created: Instant,
    val updated: Instant
)
