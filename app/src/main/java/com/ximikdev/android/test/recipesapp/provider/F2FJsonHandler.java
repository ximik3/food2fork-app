package com.ximikdev.android.test.recipesapp.provider;

import android.content.ContentValues;
import android.util.Log;

import com.ximikdev.android.test.recipesapp.database.F2FTable;
import com.ximikdev.android.test.recipesapp.restframework.StringResponseHandler;
import com.ximikdev.android.test.recipesapp.restframework.URLResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * This class converts Json Response to List to insert into database
 */
public class F2FJsonHandler implements URLResponseHandler<List<ContentValues>> {
    private StringResponseHandler stringHandler;

    /**
     * Create an instance of F2FImageHandler
     * @param stringHandler String handler
     * @see StringResponseHandler
     */
    public F2FJsonHandler(StringResponseHandler stringHandler) {
        this.stringHandler = stringHandler;
    }

    /**
     * Handle JSON request
     * @param url requested JSON URL
     * @return List of parsed JSON data
     * @throws IOException
     */
    @Override
    public List<ContentValues> handleResponse(URL url) throws IOException {
        String jsonString = stringHandler.handleResponse(url);
        F2FUri fUri = F2FUri.parse(url.toString());

        try {
            if (fUri.getAction().equals(F2FUri.GET)) {
                return getJsonToValues(jsonString);
            } else {
                return searchJsonToValues(jsonString, url);
            }
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Analyse <code>recipes</code>
     * (<a href="http://food2fork.com/api/search">http://food2fork.com/api/search</a>)
     * JSON response and extract content
     * @param jsonString input JSON
     * @param url needed to fill F2FTable.Q
     * @return list of F2FTable rows
     * @throws JSONException
     */
    public List<ContentValues> searchJsonToValues(String jsonString, URL url) throws JSONException {

        JSONArray recipes = new JSONObject(jsonString).getJSONArray(F2FTable.RECIPES_ARR);

        List<ContentValues> table = new LinkedList<>();
        for (int i = 0; i < recipes.length(); i++) {

            ContentValues newRow = new ContentValues();
            JSONObject jsonRow = recipes.getJSONObject(i);

            // Find matching values
            newRow.put(F2FTable.PUBLISHER, jsonRow.optString(F2FTable.PUBLISHER));
            newRow.put(F2FTable.F2F_URL, jsonRow.optString(F2FTable.F2F_URL));
            newRow.put(F2FTable.TITLE, jsonRow.optString(F2FTable.TITLE));
            newRow.put(F2FTable.SOURCE_URL, jsonRow.optString(F2FTable.SOURCE_URL));
            newRow.put(F2FTable.RECIPE_ID, jsonRow.optString(F2FTable.RECIPE_ID));
            newRow.put(F2FTable.IMAGE_URL, jsonRow.optString(F2FTable.IMAGE_URL));
            newRow.put(F2FTable.SOCIAL_RANK, jsonRow.optDouble(F2FTable.SOCIAL_RANK));
            newRow.put(F2FTable.PUBLISHER_URL, jsonRow.optString(F2FTable.PUBLISHER_URL));
            // Concat ingredients to String with '\n' separator
            JSONArray ingredientsArray = jsonRow.optJSONArray(F2FTable.INGREDIENTS);
            if (ingredientsArray != null) {
                StringBuilder ingredients = new StringBuilder();
                for (int j = 0; j < ingredientsArray.length(); j++) {
                    ingredients.append(ingredientsArray.optString(j));
                    ingredients.append('\n');
                }
                newRow.put(F2FTable.INGREDIENTS, ingredients.toString());
            }
            // Parse url to fill other data
            F2FUri fUri = F2FUri.parse(url.toString());
            if (fUri.getAction().equals(F2FUri.SEARCH)) {
                boolean trending = fUri.getSort().equals(F2FUri.TRENDING);
                if (fUri.hasQ()) {      // search with keywords
                    newRow.put(F2FTable.Q, fUri.getQ());    // save keywords
                    newRow.put(trending
                                    ? F2FTable.Q_TRENDING_POSITION
                                    : F2FTable.Q_RATING_POSITION,
                            F2FContentProvider.RESULTS_PER_PAGE * fUri.getPage()
                                    + (i + 1)   // current JSONArray starts with 0 but
                                                // positions are positive
                    );
                } else {    // default search call
                    newRow.put(trending
                                    ? F2FTable.TRENDING_POSITION
                                    : F2FTable.RATING_POSITION,
                            F2FContentProvider.RESULTS_PER_PAGE * fUri.getPage()
                                    + (i + 1)
                    );
                }
            }
            table.add(newRow);
        }

        return table;
    }

    /**
     * Analyse <code>recipe</code>
     * (<a href="http://food2fork.com/api/get">http://food2fork.com/api/get</a>)
     * JSON response and extract content
     * @param jsonString input JSON
     * @return list of F2FTable rows
     * @throws JSONException
     */
    public List<ContentValues> getJsonToValues(String jsonString) throws JSONException {

        Log.d(F2FContentProvider._TAG, "get json: " + jsonString);
        JSONObject recipe = new JSONObject(jsonString).getJSONObject(F2FTable.RECIPE);

        List<ContentValues> table = new LinkedList<>();
        ContentValues newRow = new ContentValues();

        // Find matching values
        newRow.put(F2FTable.PUBLISHER, recipe.optString(F2FTable.PUBLISHER));
        newRow.put(F2FTable.F2F_URL, recipe.optString(F2FTable.F2F_URL));

        JSONArray ingredientsArray = recipe.optJSONArray(F2FTable.INGREDIENTS);
        StringBuilder ingredients = new StringBuilder();
        // Concat ingredients to String with '\n' separator
        for (int j = 0; j < ingredientsArray.length(); j++) {
            ingredients.append(ingredientsArray.optString(j));
            ingredients.append('\n');
        }
        newRow.put(F2FTable.INGREDIENTS, ingredients.toString());

        newRow.put(F2FTable.SOURCE_URL, recipe.optString(F2FTable.SOURCE_URL));
        newRow.put(F2FTable.RECIPE_ID, recipe.optString(F2FTable.RECIPE_ID));
        newRow.put(F2FTable.IMAGE_URL, recipe.optString(F2FTable.IMAGE_URL));
        newRow.put(F2FTable.SOCIAL_RANK, recipe.optDouble(F2FTable.SOCIAL_RANK));
        newRow.put(F2FTable.PUBLISHER_URL, recipe.optString(F2FTable.PUBLISHER_URL));
        newRow.put(F2FTable.TITLE, recipe.optString(F2FTable.TITLE));

        table.add(newRow);
        return table;
    }
}
