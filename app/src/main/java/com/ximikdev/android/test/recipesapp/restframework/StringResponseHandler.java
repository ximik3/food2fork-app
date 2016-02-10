package com.ximikdev.android.test.recipesapp.restframework;

import android.util.Log;

import com.ximikdev.android.test.recipesapp.provider.F2FContentProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Handle URL response as text
 */
public class StringResponseHandler implements URLResponseHandler<String> {

    @Override
    public String handleResponse(URL url) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

        StringBuilder builder = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null)
        {
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
    }
}
