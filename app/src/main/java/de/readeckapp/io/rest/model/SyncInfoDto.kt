package de.readeckapp.io.rest.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SyncInfoDto(
    val id: String,
    val time: Instant,
    val type: SyncInfoType,
)

@Serializable
enum class SyncInfoType {
    update, delete
}
