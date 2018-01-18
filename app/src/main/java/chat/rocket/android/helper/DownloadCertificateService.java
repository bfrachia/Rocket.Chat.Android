package chat.rocket.android.helper;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Bruno Frachia on 1/17/18.
 */

public class DownloadCertificateService extends IntentService {

    public static final int UPDATE_DOWNLOAD_PROGRESS = 1;
    public static final int INVALID_URL = 2;

    public DownloadCertificateService() {
        super("DownloadCertService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String urlToDownload = intent.getStringExtra("URL");
            String localPath = intent.getStringExtra("localPath");
            ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
            try {
                URL url = new URL(urlToDownload);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(localPath);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    Bundle resultData = new Bundle();
                    resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                    receiver.send(UPDATE_DOWNLOAD_PROGRESS, resultData);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                Bundle resultData = new Bundle();
                resultData.putInt("progress" ,100);
                receiver.send(UPDATE_DOWNLOAD_PROGRESS, resultData);
            } catch (IOException e) {
                receiver.send(INVALID_URL, null);
            }

        }

    }
}
