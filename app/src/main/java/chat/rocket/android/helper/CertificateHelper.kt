package chat.rocket.android.helper

import android.content.Context
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import java.io.*
import java.security.KeyStore


/**
 * Created by Bruno Frachia on 1/16/18.
 */

class CertificateHelper {

    companion object {
        fun getCertFile(context: Context): InputStream? {
            return try {
                FileInputStream(File(context.filesDir.absolutePath + "/clientCert.p12"))
            }
            catch (e: FileNotFoundException) {
                null
            }
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