package de.readeckapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkListRoute(val sharedUrl: String? = null)

@Serializable
data class BookmarkDetailRoute(val bookmarkId: String)

@Serializable
object AccountSettingsRoute

@Serializable
object SettingsRoute

@Serializable
object OpenSourceLibrariesRoute

@Serializable
object LogViewRoute

@Serializable
object SyncSettingsRoute
