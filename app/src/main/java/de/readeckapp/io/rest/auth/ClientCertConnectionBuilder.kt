package de.readeckapp.io.rest.auth

import de.readeckapp.io.rest.ssl.SSLConfigurationProvider
import net.openid.appauth.connectivity.ConnectionBuilder
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

/**
 * ConnectionBuilder for AppAuth's AuthorizationService. AppAuth does its own HTTP
 * (discovery, dynamic client registration, token exchange) via a raw HttpURLConnection,
 * bypassing the shared OkHttpClient entirely, so it needs the same client-certificate
 * SSLSocketFactory applied here too, or servers that require mTLS at the TLS layer
 * (HAProxy `verify required`) reject it with a TLSv1.3 "certificate_required" alert.
 */
class ClientCertConnectionBuilder(
    private val sslConfigurationProvider: SSLConfigurationProvider
) : ConnectionBuilder {
    private val CONNECTION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15).toInt()
    private val READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10).toInt()

    override fun openConnection(uri: android.net.Uri): HttpURLConnection {
        val url = URL(uri.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = CONNECTION_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.instanceFollowRedirects = false

        if (conn is HttpsURLConnection) {
            try {
                conn.sslSocketFactory = sslConfigurationProvider.createSSLSocketFactory()
            } catch (e: Exception) {
                Timber.e(e, "Failed to configure SSL with client certificates")
            }
        }

        return conn
    }
}
