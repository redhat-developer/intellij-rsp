/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.rsp.download;

import com.intellij.openapi.progress.ProgressIndicator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Download a remote file
 */
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
