package de.readeckapp.io.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStoreImpl @Inject constructor(@ApplicationContext private val context: Context) :
    SettingsDataStore {

    private val encryptedSharedPreferences = EncryptionHelper.getEncryptedSharedPreferences(context)

    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_URL = stringPreferencesKey("url")
    private val KEY_PASSWORD = stringPreferencesKey("password")
    private val KEY_LAST_BOOKMARK_TIMESTAMP = stringPreferencesKey("lastBookmarkTimestamp")
    private val KEY_INITIAL_SYNC_PERFORMED = "initial_sync_performed"

    override fun saveUsername(username: String) {
        Timber.d("saveUsername")
        encryptedSharedPreferences.edit {
            putString(KEY_USERNAME.name, username)
        }
    }

    override fun savePassword(password: String) {
        Timber.d("savePassword")
        encryptedSharedPreferences.edit {
            putString(KEY_PASSWORD.name, password)
        }
    }

    override fun saveToken(token: String) {
        Timber.d("saveToken")
        encryptedSharedPreferences.edit {
            putString(KEY_TOKEN.name, token)
        }
    }

    override fun saveUrl(url: String) {
        Timber.d("saveUrl")
        encryptedSharedPreferences.edit {
            putString(KEY_URL.name, url)
        }
    }
    override suspend fun saveLastBookmarkTimestamp(timestamp: String) {
        encryptedSharedPreferences.edit {
            putString(KEY_LAST_BOOKMARK_TIMESTAMP.name, timestamp)
        }
    }

    override suspend fun getLastBookmarkTimestamp(): String? {
        return encryptedSharedPreferences.getString(KEY_LAST_BOOKMARK_TIMESTAMP.name, null)
    }

    override suspend fun setInitialSyncPerformed(performed: Boolean) {
        encryptedSharedPreferences.edit {
            putBoolean(KEY_INITIAL_SYNC_PERFORMED, performed)
        }
    }

    override suspend fun isInitialSyncPerformed(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_INITIAL_SYNC_PERFORMED, false)
    }

    override val tokenFlow = getStringFlow(KEY_TOKEN.name, null)
    override val usernameFlow = getStringFlow(KEY_USERNAME.name, null)
    override val urlFlow = getStringFlow(KEY_URL.name, null)
    override val passwordFlow = getStringFlow(KEY_PASSWORD.name, null)
    override suspend fun clearCredentials() {
        Timber.d("clearCredentials")
        encryptedSharedPreferences.edit(commit = true) {
            remove(KEY_USERNAME.name)
            remove(KEY_PASSWORD.name)
            remove(KEY_TOKEN.name)
            remove(KEY_URL.name)
        }
    }

    override suspend fun saveCredentials(
        url: String,
        username: String,
        password: String,
        token: String
    ) {
        Timber.d("saveCredentials")
        encryptedSharedPreferences.edit {
            putString(KEY_URL.name, url)
            putString(KEY_USERNAME.name, username)
            putString(KEY_PASSWORD.name, password)
            putString(KEY_TOKEN.name, token)
        }
    }

    private fun getStringFlow(key: String, defaultValue: String? = null): StateFlow<String?> =
        preferenceFlow(key) { encryptedSharedPreferences.getString(key, defaultValue) }

    private fun <T> preferenceFlow(key: String, getValue: () -> T): StateFlow<T> { // Create our flow using callbackflow
        // Emit initial value when we start collecting from this flow (if it exists) or use default one from params in function call above!  This is important so consumers know initial state!  Can skip this and just send updates if you do not need initial state emission on subscribe time!  That could be fine too depending on your use case - remember that!  Also you can send null as the "initial" value as well if you want!
        val state = MutableStateFlow(getValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {  // Only send updates for this specific key
                Timber.d("pref changed key=$key")
                val value = getValue()
                state.value = value
            }
        }

        encryptedSharedPreferences.registerOnSharedPreferenceChangeListener(listener) // Register the listener
        return state.asStateFlow()
    }
}
