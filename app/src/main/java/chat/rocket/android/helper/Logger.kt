package chat.rocket.android.helper

import chat.rocket.android.BuildConfig
import com.google.firebase.crash.FirebaseCrash

object Logger {

    fun report(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }
        FirebaseCrash.report(throwable)
    }
}
