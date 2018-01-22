package chat.rocket.android;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.RocketChatPersistenceRealm;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.OkHttpClient;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {

    private static RocketChatApplication instance;

    public static RocketChatApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RocketChatCache.INSTANCE.initialize(this);
        JobManager.create(this).addJobCreator(new RocketChatJobCreator());
//        OkHttpHelper.INSTANCE.getClientForWebSocket(new OkHttpHelper.GetHttpClientListener() {
//            @Override
//            public void onHttpClientRetrieved(@Nullable OkHttpClient httpClient) {
//                DDPClient.initialize(httpClient);
//            }
//        });

//        DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());
        Fabric.with(this, new Crashlytics());

        RocketChatPersistenceRealm.init(this);

        List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
        for (ServerInfo serverInfo : serverInfoList) {
            RealmStore.put(serverInfo.getHostname());
        }

//        OkHttpHelper.INSTANCE.getClientForDownloadFile(new OkHttpHelper.GetHttpClientListener() {
//            @Override
//            public void onHttpClientRetrieved(@Nullable OkHttpClient httpClient) {
//                RocketChatWidgets.initialize(RocketChatApplication.this, httpClient);
//            }
//        });

//        RocketChatWidgets.initialize(this, OkHttpHelper.INSTANCE.getClientForDownloadFile());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Logger.INSTANCE.report(e);
        });

    }
}