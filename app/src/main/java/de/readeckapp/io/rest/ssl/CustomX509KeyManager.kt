package de.readeckapp.io.rest.ssl

import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager

/**
 * Custom X509KeyManager that integrates with Android KeyChain for client certificate authentication.
 * This manager is called during SSL/TLS handshake when the server requests a client certificate.
 */
class CustomX509KeyManager(
    private val certificateManager: ClientCertificateManager
) : X509ExtendedKeyManager() {

    /**
     * Choose an alias to authenticate the client side of a secure socket.
     * This is called during SSL handshake when server requests client certificate.
     */
    override fun chooseClientAlias(
        keyType: Array<out String>?,
        issuers: Array<out Principal>?,
        socket: Socket?
    ): String? {
        return runBlocking {
            val alias = certificateManager.getCertificateAlias()
            Timber.d("chooseClientAlias called, returning: $alias")
            alias
        }
    }

    /**
     * Choose an alias to authenticate the client side of an engine.
     * This is the extended version for SSLEngine.
     */
    override fun chooseEngineClientAlias(
        keyType: Array<out String>?,
        issuers: Array<out Principal>?,
        engine: SSLEngine?
    ): String? {
        return runBlocking {
            val alias = certificateManager.getCertificateAlias()
            Timber.d("chooseEngineClientAlias called, returning: $alias")
            alias
        }
    }

    /**
     * Get the certificate chain for the given alias.
     */
    override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
        if (alias == null) {
            Timber.w("getCertificateChain called with null alias")
            return null
        }
        
        return runBlocking {
            val chain = certificateManager.getCertificateChain(alias)
            Timber.d("Retrieved certificate chain for $alias: ${chain?.size ?: 0} certificates")
            chain
        }
    }

    /**
     * Get the private key for the given alias.
     */
    override fun getPrivateKey(alias: String?): PrivateKey? {
        if (alias == null) {
            Timber.w("getPrivateKey called with null alias")
            return null
        }
        
        return runBlocking {
            val key = certificateManager.getPrivateKey(alias)
            Timber.d("Retrieved private key for $alias: ${key != null}")
            key
        }
    }

    // Server-side methods - not used for client authentication
    override fun chooseServerAlias(
        keyType: String?,
        issuers: Array<out Principal>?,
        socket: Socket?
    ): String? = null

    override fun chooseEngineServerAlias(
        keyType: String?,
        issuers: Array<out Principal>?,
        engine: SSLEngine?
    ): String? = null

    override fun getClientAliases(
        keyType: String?,
        issuers: Array<out Principal>?
    ): Array<String>? {
        return runBlocking {
            val alias = certificateManager.getCertificateAlias()
            if (alias != null) arrayOf(alias) else null
        }
    }

    override fun getServerAliases(
        keyType: String?,
        issuers: Array<out Principal>?
    ): Array<String>? = null
}