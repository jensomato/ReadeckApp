package de.readeckapp.io.rest.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val provider: ProviderDto,
    val user: UserDto
)

@Serializable
data class ProviderDto(
    val name: String,
    val id: String,
    val application: String,
    val roles: List<String>,
    val permissions: List<String>
)

@Serializable
data class UserDto(
    val username: String,
    val email: String,
    val created: Instant,
    val updated: Instant,
    val settings: SettingsDto
)

@Serializable
data class SettingsDto(
    @SerialName("debug_info")
    val debugInfo: Boolean,
    val lang: String,
    @SerialName("reader_settings")
    val readerSettings: ReaderSettingsDto
)

@Serializable
data class ReaderSettingsDto(
    val width: Int,
    val font: String,
    @SerialName("font_size")
    val fontSize: Int,
    @SerialName("line_height")
    val lineHeight: Int,
    val justify: Int,
    val hyphenation: Int
)