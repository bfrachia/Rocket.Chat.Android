package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.rocket.android.R;
import chat.rocket.android.fragment.add_server.InputHostnameFragment;
import chat.rocket.android.fragment.download_cert.DownloadCertificateFragment;

public class DownloadCertificateActivity extends AbstractFragmentActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.simple_screen);
    showFragment(new DownloadCertificateFragment());
  }

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
