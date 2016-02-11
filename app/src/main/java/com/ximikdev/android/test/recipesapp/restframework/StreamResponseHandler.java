package com.ximikdev.android.test.recipesapp.restframework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Writes data from urls into a local file cache that can be referenced by a
 * database ID.
 */
public class StreamResponseHandler implements URLResponseHandler<File> {
    public static final int BUFFER_SIZE = 1024;
    private File mCacheDir;
    private String mFileName;    // cache file name

    /**
     * Creates handler that save URL input stream into file
     *
     * @param cacheDir file directory
     * @param fileName filename where to save request
     */
    public StreamResponseHandler(File cacheDir, String fileName) {
        mFileName = fileName;
        mCacheDir = cacheDir;
    }

    /**
     * Opens URL stream and save binary data to file.
     *
     * @param url request URL
     * @return file which contain request
     * @throws IOException
     * @see URLResponseHandler#handleResponse(URL)
     */
    @Override
    public File handleResponse(URL url) throws IOException {

        InputStream input = url.openStream();

        File file = new File(mCacheDir, mFileName);
        OutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
            output.write(buffer, 0, bytesRead);
        }

        // Close streams
        output.close();
        input.close();
        return file;
    }
}
