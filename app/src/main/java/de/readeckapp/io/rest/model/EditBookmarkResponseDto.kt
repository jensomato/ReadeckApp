package de.readeckapp.io.rest.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EditBookmarkResponseDto(
    val href: String,
    val id: String,
    @SerialName("is_archived")
    val isArchived: Boolean? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean? = null,
    @SerialName("is_marked")
    val isMarked: Boolean? = null,
    val labels: String? = null,
    @SerialName("read_anchor")
    val readAnchor: String? = null,
    @SerialName("read_progress")
    val readProgress: Int? = null,
    val title: String? = null,
    val updated: Instant
)
