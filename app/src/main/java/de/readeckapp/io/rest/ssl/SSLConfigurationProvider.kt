package de.readeckapp.io.rest.ssl

import timber.log.Timber
import java.security.KeyStore
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Provides SSL configuration for OkHttp with client certificate support.
 * Creates SSLSocketFactory and TrustManager configured for mTLS authentication.
 */
@Singleton
class SSLConfigurationProvider @Inject constructor(
    private val certificateManager: ClientCertificateManager
) {
    
    /**
     * Creates an SSLSocketFactory configured with custom KeyManager for client certificates
     * and system TrustManager for server certificate validation.
     */
    fun createSSLSocketFactory(): SSLSocketFactory {
        try {
            Timber.d("Creating SSL socket factory with client certificate support")
            
            // Create custom KeyManager for client certificates
            val keyManager = CustomX509KeyManager(certificateManager)
            
            // Use system TrustManager for server certificate validation
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(null as KeyStore?)
            
            // Create SSLContext with both KeyManager and TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(
                arrayOf(keyManager),
                trustManagerFactory.trustManagers,
                SecureRandom()
            )
            
            Timber.d("SSL socket factory created successfully")
            return sslContext.socketFactory
        } catch (e: Exception) {
            Timber.e(e, "Failed to create SSL socket factory")
            throw RuntimeException("Failed to configure SSL for client certificates", e)
        }
    }
    
    /**
     * Creates a TrustManager for server certificate validation.
     * Uses the system's default trust store.
     */
    fun createTrustManager(): X509TrustManager {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(null as KeyStore?)
            
            val trustManagers = trustManagerFactory.trustManagers
            check(trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
                "Unexpected default trust managers: ${trustManagers.contentToString()}"
            }
            
            return trustManagers[0] as X509TrustManager
        } catch (e: Exception) {
            Timber.e(e, "Failed to create trust manager")
            throw RuntimeException("Failed to configure SSL trust manager", e)
        }
    }
}