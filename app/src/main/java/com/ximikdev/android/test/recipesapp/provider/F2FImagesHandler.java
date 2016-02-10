package com.ximikdev.android.test.recipesapp.provider;

import com.ximikdev.android.test.recipesapp.restframework.StreamResponseHandler;
import com.ximikdev.android.test.recipesapp.restframework.URLResponseHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Images loader
 * Encapsulate {@link StreamResponseHandler}
 */
public class F2FImagesHandler implements URLResponseHandler<File>{
    private File imagesDir;

    public F2FImagesHandler(File imagesDir) {
        this.imagesDir = imagesDir;
    }

    @Override
    public File handleResponse(URL url) throws IOException {
        StreamResponseHandler handler = new StreamResponseHandler(imagesDir, nameFromURL(url));

        return handler.handleResponse(url);
    }

    public static String nameFromURL(URL url) {
        String name = url.toString();
        int lastSlash = name.lastIndexOf('/') + 1;

        return name.substring(lastSlash, name.length());
    }
}
