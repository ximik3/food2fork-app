package com.ximikdev.android.test.recipesapp.provider;

import com.ximikdev.android.test.recipesapp.restframework.StreamResponseHandler;
import com.ximikdev.android.test.recipesapp.restframework.URLResponseHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Images loader
 * {@link StreamResponseHandler} wrapper
 */
public class F2FImagesHandler implements URLResponseHandler<File> {
    private File imagesDir;

    /**
     * Create an instance of F2FImageHandler
     *
     * @param imagesDir images download directory
     */
    public F2FImagesHandler(File imagesDir) {
        this.imagesDir = imagesDir;
    }

    /**
     * @see StreamResponseHandler#handleResponse(URL)
     * @see URLResponseHandler#handleResponse(URL)
     */
    @Override
    public File handleResponse(URL url) throws IOException {
        StreamResponseHandler handler = new StreamResponseHandler(imagesDir, nameFromURL(url));

        return handler.handleResponse(url);
    }

    /**
     * URL image filename picker. Pick everything after last '/' in URL
     *
     * @param url image url
     * @return image name
     */
    public static String nameFromURL(URL url) {
        String name = url.toString();
        int lastSlash = name.lastIndexOf('/') + 1;

        return name.substring(lastSlash, name.length());
    }
}
