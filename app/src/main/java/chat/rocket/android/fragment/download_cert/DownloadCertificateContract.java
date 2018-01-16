package chat.rocket.android.fragment.download_cert;

import chat.rocket.android.shared.BaseContract;

public interface DownloadCertificateContract {

  interface View extends BaseContract.View {
    void showLoader();

    void hideLoader(Boolean isValidServerUrl);

    void showInvalidUrlError();

    void showConnectionError();

    void showHome();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void attemptDownload(String certificateUrl);
  }

}
