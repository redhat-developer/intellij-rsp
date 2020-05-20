package org.jboss.tools.intellij.rsp.download;

import com.intellij.openapi.progress.ProgressIndicator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadUtility {

    public void download(String url, Path dlFilePath, ProgressIndicator progressIndicator) throws IOException {
        OkHttpClient client = NetworkUtils.getClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        downloadFile(response.body().byteStream(), dlFilePath, progressIndicator, response.body().contentLength());
    }

    public void uncompress(Path dlFilePath, Path destinationFolder) throws IOException {
        new UnzipUtility(dlFilePath.toFile()).extract(destinationFolder.toFile());
    }


    private static void downloadFile(InputStream input, Path dlFileName,
                                     ProgressIndicator progressIndicator, long size) throws IOException {
        byte[] buffer = new byte[4096];
        Files.createDirectories(dlFileName.getParent());
        try (OutputStream output = Files.newOutputStream(dlFileName)) {
            int lg;
            long accumulated = 0;
            while (((lg = input.read(buffer)) > 0) && !progressIndicator.isCanceled()) {
                output.write(buffer, 0, lg);
                accumulated += lg;
                progressIndicator.setFraction((double) accumulated / size);
            }
        }
    }
}
