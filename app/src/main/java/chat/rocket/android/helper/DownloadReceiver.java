package chat.rocket.android.helper;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Bruno Frachia on 1/17/18.
 */

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

        if (resultCode == DownloadCertificateService.UPDATE_DOWNLOAD_PROGRESS) {
            if (resultData.getInt("progress") == 100) {
//                if (downloadListener != null) {
//                    downloadListener.onDownloadCompleted();
//                }
            }
        }
    }
}
