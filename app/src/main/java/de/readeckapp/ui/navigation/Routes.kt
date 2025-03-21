package de.readeckapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object BookmarkListRoute

@Serializable
data class BookmarkDetailRoute(val bookmarkId: String)

@Serializable
object AccountSettingsRoute

@Serializable
object SettingsRoute

@Serializable
object OpenSourceLibrariesRoute
