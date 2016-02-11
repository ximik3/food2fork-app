package com.ximikdev.android.test.recipesapp.restframework;

import java.io.IOException;
import java.net.URL;

/**
 * Custom URL response handler interface
 * @param <T> custom return type
 */
public interface URLResponseHandler<T> {
    /**
     * Handle URL response and return result in generic type
     * @param url request URL
     * @return handled result
     * @throws IOException
     */
    T handleResponse(URL url) throws IOException;
}
