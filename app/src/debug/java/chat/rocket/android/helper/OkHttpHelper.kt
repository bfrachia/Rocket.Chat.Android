package chat.rocket.android.helper

import android.content.Context
import android.security.KeyChain
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import chat.rocket.android.api.rest.CookieInterceptor
import chat.rocket.android.api.rest.DefaultCookieProvider
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object OkHttpHelper {

    interface GenerateSSLContextListener {
        fun onSSLContextGenerated(sslContext: SSLContext?)
    }

    interface GetHttpClientListener {
        fun onHttpClientRetrieved(httpClient: OkHttpClient?)
    }

    @Throws(IOException::class)
    private fun generateSSLContext(context: Context, listener: GenerateSSLContextListener) {
            val alias = RocketChatCache.getCertAlias()

            if (alias != null) {
                doAsync(exceptionHandler = { e -> e.printStackTrace()}) {
                    try {

                        val certificates = KeyChain.getCertificateChain(context, alias)


                        val pk = KeyChain.getPrivateKey(context, alias)


                        val trustStore = KeyStore.getInstance(KeyStore
                                .getDefaultType())


                        val keyManager = object : X509ExtendedKeyManager() {

                            override fun chooseClientAlias(strings: Array<String>, principals: Array<Principal>?, socket: Socket): String {
                                return alias
                            }

                            override fun chooseServerAlias(s: String, principals: Array<Principal>, socket: Socket): String {
                                return alias
                            }

                            override fun getCertificateChain(s: String): Array<X509Certificate>? {
                                return certificates
                            }

                            override fun getClientAliases(s: String, principals: Array<Principal>): Array<String> {
                                return arrayOf(alias)
                            }

                            override fun getServerAliases(s: String, principals: Array<Principal>): Array<String> {
                                return arrayOf(alias)
                            }

                            override fun getPrivateKey(s: String): PrivateKey? {
                                return pk
                            }
                        }

                        val trustFactory = TrustManagerFactory
                                .getInstance(TrustManagerFactory.getDefaultAlgorithm())

                        trustFactory.init(trustStore)

                        val tm = arrayOf<X509TrustManager>(object : X509TrustManager {
                            @Throws(CertificateException::class)
                            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                            }

                            @Throws(CertificateException::class)
                            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate> {
                                return certificates
                            }

                            fun isClientTrusted(arg0: Array<X509Certificate>): Boolean {
                                return true
                            }

                            fun isServerTrusted(arg0: Array<X509Certificate>): Boolean {
                                return true
                            }

                        })

                        val sslContext = SSLContext.getInstance("TLS")
                        sslContext.init(arrayOf<KeyManager>(keyManager), tm, null)
                        SSLContext.setDefault(sslContext)

                        uiThread {
                            listener.onSSLContextGenerated(sslContext)
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            else throw IOException()
    }

    @Throws(IOException::class)
    fun getClient(listener: GetHttpClientListener) {
        if (httpClient == null) {

            try {
                generateSSLContext(RocketChatApplication.getInstance().applicationContext,
                        object: GenerateSSLContextListener {
                            override fun onSSLContextGenerated(sslContext: SSLContext?) {
                                httpClient = OkHttpClient.Builder().sslSocketFactory(sslContext?.socketFactory).build()
                                listener.onHttpClientRetrieved(httpClient)
                            }
                        })

            } catch (e: IOException) {
                httpClient = OkHttpClient.Builder().build()
                listener.onHttpClientRetrieved(httpClient)
            }

        }
        else {
            listener.onHttpClientRetrieved(httpClient)
        }
    }

    fun getClientForUploadFile(listener: GetHttpClientListener) {
        if (httpClientForUploadFile == null) {

            try {
                generateSSLContext(RocketChatApplication.getInstance().applicationContext,
                        object: GenerateSSLContextListener {
                            override fun onSSLContextGenerated(sslContext: SSLContext?) {
                                httpClientForUploadFile = OkHttpClient.Builder().sslSocketFactory(sslContext?.socketFactory).build()
                                listener.onHttpClientRetrieved(httpClientForUploadFile)
                            }
                        })

            } catch (e: IOException) {
                httpClientForUploadFile = OkHttpClient.Builder().build()
                listener.onHttpClientRetrieved(httpClientForUploadFile)
            }
        }
        else {
            listener.onHttpClientRetrieved(httpClientForUploadFile)
        }
    }

    fun getClientForDownloadFile(listener: GetHttpClientListener) {
        if (httpClientForDownloadFile == null) {


            try {
                generateSSLContext(RocketChatApplication.getInstance().applicationContext,
                        object: GenerateSSLContextListener {
                            override fun onSSLContextGenerated(sslContext: SSLContext?) {
                                httpClientForDownloadFile = OkHttpClient.Builder()
                                        .addNetworkInterceptor(StethoInterceptor())
                                        .followRedirects(true)
                                        .followSslRedirects(true)
                                        .addInterceptor(CookieInterceptor(DefaultCookieProvider()))
                                        .sslSocketFactory(sslContext?.socketFactory)
                                        .build()

                                listener.onHttpClientRetrieved(httpClientForDownloadFile)
                            }
                        })

            } catch (e: IOException) {
                httpClientForDownloadFile = OkHttpClient.Builder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .addInterceptor(CookieInterceptor(DefaultCookieProvider()))
                        .build()
                listener.onHttpClientRetrieved(httpClientForDownloadFile)
            }

        }
        else {
            listener.onHttpClientRetrieved(httpClientForDownloadFile)
        }
    }

    /**
     * Returns the OkHttpClient instance for WebSocket connection.
     * @return The OkHttpClient WebSocket connection instance.
     */
    fun getClientForWebSocket(listener: GetHttpClientListener) {
        if (httpClientForWS == null) {

            try {
                generateSSLContext(RocketChatApplication.getInstance().applicationContext,
                        object: GenerateSSLContextListener {
                            override fun onSSLContextGenerated(sslContext: SSLContext?) {
                                httpClientForWS =  OkHttpClient.Builder()
                                        .sslSocketFactory(sslContext?.socketFactory)
                                        .readTimeout(100, TimeUnit.SECONDS)
                                        .build()
                                listener.onHttpClientRetrieved(httpClientForWS)
                            }
                        })

            } catch (e: IOException) {
                httpClientForWS = OkHttpClient.Builder()
                        .readTimeout(100, TimeUnit.SECONDS)
                        .build()
                listener.onHttpClientRetrieved(httpClientForWS)
            }

        }
        else {
            listener.onHttpClientRetrieved(httpClientForWS)
        }
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