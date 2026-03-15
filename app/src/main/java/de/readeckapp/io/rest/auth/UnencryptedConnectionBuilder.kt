package de.readeckapp.io.rest.auth

import android.annotation.SuppressLint
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

object UnencryptedConnectionBuilder : ConnectionBuilder {
    private val CONNECTION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15).toInt()
    private val READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10).toInt()

    @SuppressLint("NetworkSecurityConfig")
    override fun openConnection(uri: android.net.Uri): HttpURLConnection {
        val url = URL(uri.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = CONNECTION_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.instanceFollowRedirects = false
        return conn
    }
}
