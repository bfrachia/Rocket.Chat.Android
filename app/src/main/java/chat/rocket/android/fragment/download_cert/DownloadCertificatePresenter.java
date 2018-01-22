package chat.rocket.android.fragment.download_cert;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.webkit.URLUtil;

import org.jetbrains.annotations.Nullable;

import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.CertificateHelper;
import chat.rocket.android.helper.DownloadCertificateService;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android_ddp.DDPClient;
import okhttp3.OkHttpClient;

public class DownloadCertificatePresenter extends BasePresenter<DownloadCertificateContract.View> implements DownloadCertificateContract.Presenter {

  @Override
  public void attemptDownload(final String certificatePath, final String password) {
    if (URLUtil.isValidUrl(certificatePath)) {
      view.showLoader();

      RocketChatCache.INSTANCE.setCertPassword(password);

      Intent intent = new Intent(RocketChatApplication.getInstance().getApplicationContext(), DownloadCertificateService.class);
      intent.putExtra("URL", certificatePath);
      intent.putExtra("receiver", new DownloadReceiver(new Handler()));
      intent.putExtra("localPath", RocketChatApplication.getInstance().getApplicationContext().getFilesDir().getAbsolutePath() + "/clientCert.p12");
      RocketChatApplication.getInstance().getApplicationContext().startService(intent);
    }
    else {
      view.invalidCertAndPassword();
    }
  }

  public class DownloadReceiver extends ResultReceiver {

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public DownloadReceiver(Handler handler) {
      super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);

      switch (resultCode) {
        case DownloadCertificateService.UPDATE_DOWNLOAD_PROGRESS:
          if (resultData.getInt("progress") == 100) {

            if (CertificateHelper.Companion.areCertificateAndPasswordValid()) {
              OkHttpHelper.INSTANCE.resetClients();

              OkHttpHelper.INSTANCE.getClientForWebSocket(new OkHttpHelper.GetHttpClientListener() {
                @Override
                public void onHttpClientRetrieved(@Nullable OkHttpClient httpClient) {
                  DDPClient.initialize(httpClient);
                }
              });

//              DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());

              OkHttpHelper.INSTANCE.getClientForDownloadFile(new OkHttpHelper.GetHttpClientListener() {
                @Override
                public void onHttpClientRetrieved(@Nullable OkHttpClient httpClient) {
                  RocketChatWidgets.initialize(RocketChatApplication.getInstance().getApplicationContext(), httpClient);
                }
              });

//              RocketChatWidgets.initialize(RocketChatApplication.getInstance().getApplicationContext(), OkHttpHelper.INSTANCE.getClientForDownloadFile());
              view.showAddServerActivity();
            }
            else {
              RocketChatCache.INSTANCE.setCertPassword(null);
              view.invalidCertAndPassword();
            }
          }
          break;

        case DownloadCertificateService.INVALID_URL:
          view.showInvalidUrlError();
          break;
      }
    }
  }

}