package chat.rocket.android.helper

import android.content.Context
import android.security.KeyChain
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import java.io.*
import java.security.KeyStore
import android.security.KeyChain.getPrivateKey
import android.security.KeyChain.getCertificateChain
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager


/**
 * Created by Bruno Frachia on 1/16/18.
 */

class CertificateHelper {

    companion object {
        fun getCertFile(context: Context): InputStream? {
//            return try {
//                FileInputStream(File(context.filesDir.absolutePath + "/clientCert.p12"))
//            }
//            catch (e: FileNotFoundException) {
//                null
//            }

//            val alias = "1"
//
//            val certificates = KeyChain.getCertificateChain(context, alias)
//
//            val pk = getPrivateKey(context,alias)
//
//
//            val trustStore = KeyStore.getInstance(KeyStore
//                    .getDefaultType())
//
//
//            val keyManager = object : X509ExtendedKeyManager() {
//
//                override fun chooseClientAlias(strings: Array<String>, principals: Array<Principal>, socket: Socket): String {
//                    return alias
//                }
//
//                override fun chooseServerAlias(s: String, principals: Array<Principal>, socket: Socket): String {
//                    return alias
//                }
//
//                override fun getCertificateChain(s: String): Array<X509Certificate>? {
//                    return certificates
//                }
//
//                override fun getClientAliases(s: String, principals: Array<Principal>): Array<String> {
//                    return arrayOf(alias)
//                }
//
//                override fun getServerAliases(s: String, principals: Array<Principal>): Array<String> {
//                    return arrayOf(alias)
//                }
//
//                override fun getPrivateKey(s: String): PrivateKey? {
//                    return pk
//                }
//            }
//
//            val trustFactory = TrustManagerFactory
//                    .getInstance(TrustManagerFactory.getDefaultAlgorithm())
//
//            trustFactory.init(trustStore)
//
//            val trustManagers = trustFactory.trustManagers
//
//
//            val tm = arrayOf<X509TrustManager>(object : X509TrustManager {
//                @Throws(CertificateException::class)
//                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//
//                @Throws(CertificateException::class)
//                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//
//                //            public X509Certificate[] getAcceptedIssuers() {
//                //                return certificates;
//                //            }
//
//                override fun getAcceptedIssuers(): Array<X509Certificate> {
//                    return certificates
//                }
//
//                fun isClientTrusted(arg0: Array<X509Certificate>): Boolean {
//                    return true
//                }
//
//                fun isServerTrusted(arg0: Array<X509Certificate>): Boolean {
//                    return true
//                }
//
//            })
            return null
        }

        fun hasCertificate(context: Context): Boolean {
            return try {
                FileInputStream(File(context.filesDir.absolutePath + "/clientCert.p12"))
                true
            }
            catch (e: FileNotFoundException) {
                false
            }
        }

        fun deleteCertificate(context: Context): Boolean {
            return try {
                File(context.filesDir.absolutePath + "/clientCert.p12").delete()
                true
            }
            catch (e: FileNotFoundException) {
                false
            }

        }

        fun storePassword(password: String?) {
            RocketChatCache.setCertPassword(password)
        }

        fun deleteCertificatePassword() {
            storePassword(null)
        }

        fun getPassword(): String? {
            return RocketChatCache.getCertPassword()
        }

        fun areCertificateAndPasswordValid(): Boolean {
            val keyStore = KeyStore.getInstance("PKCS12")
            val fis = getCertFile(RocketChatApplication.getInstance().applicationContext)

            return try {
                keyStore.load(fis, getPassword()?.toCharArray())
                fis?.close()
                true
            }
            catch (e: IOException) {
                false
            }
        }
    }
}