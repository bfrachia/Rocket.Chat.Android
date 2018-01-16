package chat.rocket.android.fragment.download_cert;

import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;

public class DownloadCertificatePresenter extends BasePresenter<DownloadCertificateContract.View> implements DownloadCertificateContract.Presenter {
  private final ConnectivityManagerApi connectivityManager;
  private boolean isValidServerUrl;

  public DownloadCertificatePresenter(ConnectivityManagerApi connectivityManager) {
    this.connectivityManager = connectivityManager;
  }

  @Override
  public void attemptDownload(final String certificatePath) {
    view.showLoader();
  }

}