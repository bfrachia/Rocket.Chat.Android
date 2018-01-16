package chat.rocket.android.helper

import android.content.Context
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.api.rest.CookieInterceptor
import chat.rocket.android.api.rest.DefaultCookieProvider
import chat.rocket.android.helper.CertificateHelper.Companion.getCertFile
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

object OkHttpHelper {

    private fun generateSSLContext(context: Context): SSLContext {
        val keyStore = KeyStore.getInstance("PKCS12")
        val fis = getCertFile(context)
        keyStore.load(fis, "7890".toCharArray())

        val sslContext = SSLContext.getInstance("TLS")
        val keyManagerFactory = KeyManagerFactory.getInstance("X509")
        keyManagerFactory.init(keyStore, "7890".toCharArray())
        sslContext.init(keyManagerFactory.keyManagers, null, null)

        return sslContext
    }

    fun getClient(): OkHttpClient {
        if (httpClient == null) {

            val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

            httpClient = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory).build()

        }
        return httpClient ?: throw AssertionError("httpClient set to null by another thread")
    }

    fun getClientForUploadFile(): OkHttpClient {
        if (httpClientForUploadFile == null) {

            val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

            httpClientForUploadFile = OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory)
                    .build()

        }
        return httpClientForUploadFile ?: throw AssertionError("httpClientForUploadFile set to null by another thread")
    }

    fun getClientForDownloadFile(): OkHttpClient {
        if (httpClientForDownloadFile == null) {

            val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

            httpClientForDownloadFile = OkHttpClient.Builder()
                    .addNetworkInterceptor(StethoInterceptor())
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .addInterceptor(CookieInterceptor(DefaultCookieProvider()))
                    .sslSocketFactory(sslContext.socketFactory)
                    .build()
        }
        return httpClientForDownloadFile ?: throw  AssertionError("httpClientForDownloadFile set to null by another thread")
    }

    /**
     * Returns the OkHttpClient instance for WebSocket connection.
     * @return The OkHttpClient WebSocket connection instance.
     */
    fun getClientForWebSocket(): OkHttpClient {
        if (httpClientForWS == null) {

            val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

            httpClientForWS = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()
        }
        return httpClientForWS ?: throw AssertionError("httpClientForWS set to null by another thread")
    }

    private var httpClient: OkHttpClient? = null
    private var httpClientForUploadFile: OkHttpClient? = null
    private var httpClientForDownloadFile: OkHttpClient? = null
    private var httpClientForWS: OkHttpClient? = null
}