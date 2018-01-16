package chat.rocket.android.helper

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

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
    }

}