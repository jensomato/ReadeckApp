package de.readeckapp.io.rest.ssl

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Helper class to trigger certificate selection from UI components.
 * Provides a composable function to easily integrate certificate selection into Compose UI.
 */
@Singleton
class CertificateSelectionHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val certificateManager: ClientCertificateManager
) {
    
    /**
     * Triggers the system certificate picker dialog.
     * @param activity The activity context
     * @param host The server hostname (extracted from URL)
     * @param port The server port (default 443 for HTTPS)
     * @return The selected certificate alias or null if cancelled
     */
    suspend fun selectCertificate(
        activity: Activity,
        host: String,
        port: Int = 443
    ): String? = suspendCancellableCoroutine { continuation ->
        certificateManager.chooseClientCertificate(
            activity = activity,
            host = host,
            port = port
        ) { alias ->
            Timber.d("Certificate selection completed: $alias")
            continuation.resume(alias)
        }
    }
    
    /**
     * Clears the stored certificate selection.
     */
    suspend fun clearCertificate() {
        certificateManager.clearCertificateAlias()
    }
    
    /**
     * Gets the currently selected certificate alias.
     */
    suspend fun getCurrentCertificateAlias(): String? {
        return certificateManager.getCertificateAlias()
    }
    
    /**
     * Checks if a certificate is currently configured.
     */
    suspend fun hasCertificate(): Boolean {
        return certificateManager.hasCertificate()
    }
}

/**
 * Composable function to remember the CertificateSelectionHelper.
 * This is a convenience function for use in Compose UI.
 */
@Composable
fun rememberCertificateSelectionHelper(helper: CertificateSelectionHelper): CertificateSelectionHelper {
    return remember { helper }
}