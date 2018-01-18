package chat.rocket.android.helper

import android.content.Context
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.api.rest.CookieInterceptor
import chat.rocket.android.api.rest.DefaultCookieProvider
import chat.rocket.android.helper.CertificateHelper.Companion.getCertFile
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import java.io.IOException
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

object OkHttpHelper {

    @Throws(IOException::class)
    private fun generateSSLContext(context: Context): SSLContext {
        val password = CertificateHelper.getPassword()
        if (password != null) {
            val keyStore = KeyStore.getInstance("PKCS12")
            val fis = getCertFile(context)

            try {
                keyStore.load(fis, password.toCharArray())
            }
            catch (e: IOException) {
                throw e
            }

            val sslContext = SSLContext.getInstance("TLS")
            val keyManagerFactory = KeyManagerFactory.getInstance("X509")
            keyManagerFactory.init(keyStore, password.toCharArray())
            sslContext.init(keyManagerFactory.keyManagers, null, null)
            return sslContext
        }
        else throw IOException()

    }

    @Throws(IOException::class)
    fun getClient(): OkHttpClient {
        if (httpClient == null) {

            httpClient = try {
                val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)
                OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory).build()
            } catch (e: IOException) {
                OkHttpClient.Builder().build()
            }


        }
        return httpClient ?: throw AssertionError("httpClient set to null by another thread")
    }

    fun getClientForUploadFile(): OkHttpClient {
        if (httpClientForUploadFile == null) {

            httpClientForUploadFile = try {
                val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

                OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.socketFactory)
                        .build()
            } catch (e: IOException) {
                OkHttpClient.Builder().build()
            }

        }
        return httpClientForUploadFile ?: throw AssertionError("httpClientForUploadFile set to null by another thread")
    }

    fun getClientForDownloadFile(): OkHttpClient {
        if (httpClientForDownloadFile == null) {

            httpClientForDownloadFile = try {
                val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)


                OkHttpClient.Builder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .addInterceptor(CookieInterceptor(DefaultCookieProvider()))
                        .sslSocketFactory(sslContext.socketFactory)
                        .build()

            } catch (e: IOException) {
                OkHttpClient.Builder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .addInterceptor(CookieInterceptor(DefaultCookieProvider()))
                        .build()
            }
        }
        return httpClientForDownloadFile ?: throw  AssertionError("httpClientForDownloadFile set to null by another thread")
    }

    /**
     * Returns the OkHttpClient instance for WebSocket connection.
     * @return The OkHttpClient WebSocket connection instance.
     */
    fun getClientForWebSocket(): OkHttpClient {
        if (httpClientForWS == null) {

            httpClientForWS = try {
                val sslContext = generateSSLContext(RocketChatApplication.getInstance().applicationContext)

                OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.socketFactory)
                        .readTimeout(100, TimeUnit.SECONDS)
                        .build()
            } catch (e: IOException) {
                OkHttpClient.Builder()
                        .readTimeout(100, TimeUnit.SECONDS)
                        .build()
            }

        }
        return httpClientForWS ?: throw AssertionError("httpClientForWS set to null by another thread")
    }

    fun resetClients() {
        httpClient = null
        httpClientForUploadFile = null
        httpClientForDownloadFile = null
        httpClientForWS = null
    }

    private var httpClient: OkHttpClient? = null
    private var httpClientForUploadFile: OkHttpClient? = null
    private var httpClientForDownloadFile: OkHttpClient? = null
    private var httpClientForWS: OkHttpClient? = null
}