package chat.rocket.android.fragment.download_cert;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import chat.rocket.android.BuildConfig;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.service.ConnectivityManager;

/**
 * Input server host.
 */
public class DownloadCertificateFragment extends AbstractFragment implements DownloadCertificateContract.View {

    private DownloadCertificateContract.Presenter presenter;
    private ConstraintLayout container;
    private View waitingView;

    public DownloadCertificateFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context appContext = getContext().getApplicationContext();
        presenter = new DownloadCertificatePresenter(ConnectivityManager.getInstance(appContext));
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_input_certificate_url;
    }

    @Override
    protected void onSetupView() {
        setupVersionInfo();

        container = rootView.findViewById(R.id.container);
        waitingView = rootView.findViewById(R.id.waiting);
        rootView.findViewById(R.id.btn_download).setOnClickListener(view -> handleCertDownload());
    }

    private void setupVersionInfo() {
        TextView versionInfoView = rootView.findViewById(R.id.version_info);
        versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
    }

    private void handleCertDownload() {
        hideSoftKeyboard();
        presenter.attemptDownload(getDownloadUrl(), getCertificatePassword());
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.bindView(this);
    }

    @Override
    public void onDestroyView() {
        presenter.release();
        super.onDestroyView();
    }

    private String getDownloadUrl() {
        final TextView editor = (TextView) rootView.findViewById(R.id.editor_download_url);

        return TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString().toLowerCase();
    }

    private String getCertificatePassword() {
        final TextView editor = (TextView) rootView.findViewById(R.id.editor_password);

        return TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString().toLowerCase();
    }

    private void showError(String errString) {
        Snackbar.make(rootView, errString, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoader() {
        container.setVisibility(View.GONE);
        waitingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader(Boolean isValidServerUrl) {
        if(!isValidServerUrl) {
            waitingView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showInvalidUrlError() {
        showError(getString(R.string.input_hostname_invalid_server_message));
    }

    @Override
    public void showConnectionError() {
        showError(getString(R.string.connection_error_try_later));
    }

    @Override
    public void showAddServerActivity() {
        LaunchUtil.showAddServerActivity(getContext());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void invalidCertAndPassword() {
        showError(getString(R.string.connection_error_certificate_password_combination));
        waitingView.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }

}
