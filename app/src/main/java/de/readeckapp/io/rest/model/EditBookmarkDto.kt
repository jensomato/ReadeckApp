package de.readeckapp.io.rest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EditBookmarkDto(
    @SerialName("add_labels")
    val addLabels: List<String>? = null,
    @SerialName("is_archived")
    val isArchived: Boolean? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean? = null,
    @SerialName("is_marked")
    val isMarked: Boolean? = null,
    val labels: List<String>? = null,
    @SerialName("read_anchor")
    val readAnchor: String? = null,
    @SerialName("read_progress")
    val readProgress: Int? = null,
    @SerialName("remove_labels")
    val removeLabels: List<String>? = null,
    val title: String? = null
)
