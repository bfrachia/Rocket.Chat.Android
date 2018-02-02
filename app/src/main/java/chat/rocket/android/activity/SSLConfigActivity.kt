package chat.rocket.android.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import chat.rocket.android.R
import chat.rocket.android.RocketChatCache
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.android.widget.RocketChatWidgets
import chat.rocket.android_ddp.DDPClient
import okhttp3.OkHttpClient

class SSLConfigActivity : AppCompatActivity() {

    var canPass = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sslconfig)

        val intent = Intent(this, MainActivity::class.java)

        val alias = RocketChatCache.getCertAlias()

        if (alias != null) {
            OkHttpHelper.getClientForWebSocket(object : OkHttpHelper.GetHttpClientListener {
                override fun onHttpClientRetrieved(httpClient: OkHttpClient?) {
                    DDPClient.initialize(httpClient)
                    if (canPass) {
                        startActivity(intent)
                    }
                    canPass = true
                }
            })

            OkHttpHelper.getClientForDownloadFile(object : OkHttpHelper.GetHttpClientListener {
                override fun onHttpClientRetrieved(httpClient: OkHttpClient?) {
                    RocketChatWidgets.initialize(this@SSLConfigActivity, httpClient)
                    if (canPass) {
                        startActivity(intent)
                    }
                    canPass = true
                }
            })
        }
        else {
            startActivity(intent)
        }
    }
}
