package de.readeckapp.io.prefs

import kotlinx.coroutines.flow.StateFlow

interface SettingsDataStore {
    val tokenFlow: StateFlow<String?>
    val usernameFlow: StateFlow<String?>
    val urlFlow: StateFlow<String?>
    fun saveUsername(username: String)
    fun saveToken(token: String)
    fun saveUrl(url: String)
}