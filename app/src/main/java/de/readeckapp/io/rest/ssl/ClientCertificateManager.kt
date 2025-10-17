package de.readeckapp.io.rest.ssl

import android.app.Activity
import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages client certificate selection and retrieval from Android KeyChain.
 * Handles certificate alias persistence and provides certificates for mTLS authentication.
 */
@Singleton
class ClientCertificateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        private const val CERT_ALIAS_KEY = "client_certificate_alias"
    }

    /**
     * Triggers the Android system certificate picker dialog.
     * Must be called from an Activity context.
     * 
     * @param activity The activity context for showing the picker
     * @param host The server hostname requesting the certificate
     * @param port The server port
     * @param callback Callback invoked with the selected alias (or null if cancelled)
     */
    fun chooseClientCertificate(
        activity: Activity,
        host: String,
        port: Int,
        callback: (String?) -> Unit
    ) {
        Timber.d("Requesting client certificate selection for $host:$port")
        
        KeyChain.choosePrivateKeyAlias(
            activity,
            { alias ->
                Timber.d("Certificate selected: ${alias ?: "none"}")
                if (alias != null) {
                    runBlocking {
                        saveCertificateAlias(alias)
                    }
                }
                callback(alias)
            },
            null, // keyTypes - null means accept all
            null, // issuers - null means accept all
            host,
            port,
            null  // alias - null means no pre-selection
        )
    }

    /**
     * Gets the stored certificate alias from preferences.
     * @return The certificate alias or null if not set
     */
    suspend fun getCertificateAlias(): String? {
        return settingsDataStore.getClientCertificateAlias().first()
    }

    /**
     * Saves the certificate alias to preferences.
     * @param alias The certificate alias to save
     */
    suspend fun saveCertificateAlias(alias: String) {
        Timber.d("Saving certificate alias: $alias")
        settingsDataStore.setClientCertificateAlias(alias)
    }

    /**
     * Clears the stored certificate alias.
     */
    suspend fun clearCertificateAlias() {
        Timber.d("Clearing certificate alias")
        settingsDataStore.setClientCertificateAlias(null)
    }

    /**
     * Retrieves the certificate chain for the given alias from KeyChain.
     * @param alias The certificate alias
     * @return Array of X509 certificates or null if not found
     */
    suspend fun getCertificateChain(alias: String): Array<X509Certificate>? {
        return try {
            Timber.d("Retrieving certificate chain for alias: $alias")
            KeyChain.getCertificateChain(context, alias)
        } catch (e: KeyChainException) {
            Timber.e(e, "Failed to retrieve certificate chain for alias: $alias")
            null
        } catch (e: InterruptedException) {
            Timber.e(e, "Interrupted while retrieving certificate chain")
            null
        }
    }

    /**
     * Retrieves the private key for the given alias from KeyChain.
     * @param alias The certificate alias
     * @return The private key or null if not found
     */
    suspend fun getPrivateKey(alias: String): PrivateKey? {
        return try {
            Timber.d("Retrieving private key for alias: $alias")
            KeyChain.getPrivateKey(context, alias)
        } catch (e: KeyChainException) {
            Timber.e(e, "Failed to retrieve private key for alias: $alias")
            null
        } catch (e: InterruptedException) {
            Timber.e(e, "Interrupted while retrieving private key")
            null
        }
    }

    /**
     * Checks if a certificate is currently configured.
     * @return true if a certificate alias is stored
     */
    suspend fun hasCertificate(): Boolean {
        return getCertificateAlias() != null
    }
}