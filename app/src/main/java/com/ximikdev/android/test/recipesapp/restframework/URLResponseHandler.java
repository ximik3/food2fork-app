package com.ximikdev.android.test.recipesapp.restframework;

import java.io.IOException;
import java.net.URL;

/**
 * Custom URL response handler
 * @param <T> custom return type
 */
public interface URLResponseHandler<T> {
    T handleResponse(URL url) throws IOException;
}
