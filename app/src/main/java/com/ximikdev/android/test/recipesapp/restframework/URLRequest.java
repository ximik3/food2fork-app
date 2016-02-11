package com.ximikdev.android.test.recipesapp.restframework;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

/**
 * Perform URL request asynchronously.
 */
public class URLRequest extends AsyncTask<URL, Void, Object> {

    /**
     * Listener to listen request will be completed
     */
    public interface OnCompleteListener {
        void onRequestComplete(URL url, Object response);
    }

    private URL url;
    private URLResponseHandler handler;
    private OnCompleteListener completeListener;

    /**
     * Async URL request task
     *
     * @param handler          handler to handle result
     * @param completeListener null if not specified
     */
    public URLRequest(URLResponseHandler handler, OnCompleteListener completeListener) {
        this.handler = handler;
        this.completeListener = completeListener;
    }

    @Override
    protected Object doInBackground(URL... params) {
        url = params[0];
        try {
            return handler.handleResponse(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object response) {
        super.onPostExecute(response);

        if (completeListener != null) {
            completeListener.onRequestComplete(url, response);
        }
    }
}
