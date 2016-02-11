package com.ximikdev.android.test.recipesapp.restframework;

import android.content.ContentProvider;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates functions for asynchronous REST requests so that subclass
 * content providers can use them for initiating request while still using
 * custom methods for interpreting REST based content such as, RSS, ATOM,
 * JSON, etc.
 */
public abstract class RESTContentProvider extends ContentProvider implements
        URLRequest.OnCompleteListener {
    // Currently running requests
    private final Map<URL, URLRequest> mRequestsInProgress = new HashMap<>();

    public RESTContentProvider() {
    }

    /**
     * Instantiate new asynchronous task
     *
     * @param url     url link
     * @param handler customize the way to handle requests
     * @return
     */
    private URLRequest startURLTask(URL url, URLResponseHandler handler,
                                    URLRequest.OnCompleteListener listener) {
        URLRequest task = new URLRequest(handler, listener);
        task.execute(url);
        return task;
    }

    /**
     * Add {@link URLRequest} to schedule. If exist ignore request.
     *
     * @param url
     * @param handler
     */
    public void asyncRequest(URL url, URLResponseHandler handler,
                             URLRequest.OnCompleteListener listener) {
        synchronized (mRequestsInProgress) {
            if (!mRequestsInProgress.containsKey(url)) {
                mRequestsInProgress.put(
                        url,
                        startURLTask(url, handler, listener));
            }
        }
    }

    /**
     * Remove {@link URLRequest} from schedule.
     *
     * @param url
     * @param response
     */
    @Override
    public void onRequestComplete(URL url, Object response) {
        mRequestsInProgress.remove(url);
    }
}
