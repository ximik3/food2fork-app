package com.ximikdev.android.test.recipesapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ximikdev.android.test.recipesapp.database.F2FTable;
import com.ximikdev.android.test.recipesapp.provider.F2FContentProvider;
import com.ximikdev.android.test.recipesapp.provider.F2FUri;
import com.ximikdev.android.test.recipesapp.provider.F2FUriHolder;

/**
 * Food2Fork Recipes App is made on REST interaction pattern B modification
 * presented on Google I/O 2010. The idea is to connect to RESTful web services
 * via {@link android.content.ContentProvider} API, as they has similar CRUD interface.
 *
 * <p><b>Activity</b> -- <b>ContentProvider</b> -- <b>REST Web Service</b></p>
 *
 * Activity send queries to ContentProvider and get data available in database.
 * ContentProvider starts background service, update database with new data and
 * notify cursors when new data is available
 * <br/>
 * This is main launcher Activity with ListView to show recipes
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // Log & Bundle TAGs
    private static final String _TAG = MainActivity.class.getSimpleName();
    public static final String RECIPE_ID = "com.ximikdev.android.test.food2fork.RECIPE_ID";
    public static final String QUERY_URI = "com.ximikdev.android.test.food2fork.QUERY_URI";
    // Loader identifiers
    private static final int LOADER_RECIPES = 0;

    private SimpleCursorAdapter adapter;
    private F2FUriHolder queryUri;
    //Search
    private boolean searchIsActive;
    MenuItem searchCancelIcon;
    EditText searchInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        setSupportActionBar(toolbar);
        //endregion
        //region searchInput
        searchInput = new EditText(this);
        searchInput.setHint(R.string.search_hint);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setHintTextColor(ContextCompat.getColor(this, R.color.colorSearchHint));
        searchInput.setTextColor(ContextCompat.getColor(this, R.color.colorSearchText));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        searchInput.setLayoutParams(params);
        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchInput, 0);
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                    queryUri.setQ(""); // clear search query
                }
            }
        });
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int size = s.length();
                if (size > 0 && s.charAt(size - 1) == '\n') { // Enter pressed
                    s.replace(size - 1, size, "");
                    String query = s.toString()
                            .replace(" ", ",");
                    queryUri.setQ(query);
                    Log.i(_TAG, "enter");
                    restartLoader(LOADER_RECIPES, queryUri.toString());
                }
            }
        });
        //endregion
        //region fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkToolbarSearch(searchIsActive, toolbar);
                searchIsActive = !searchIsActive;
            }
        });
        //endregion

        // Default uri. (all recipes, trending sorting)
        queryUri = new F2FUriHolder(F2FUri.parse(F2FContentProvider.SEARCH_URI));
        queryUri.setKey(F2FContentProvider.DEFAULT_API_KEY);

        ListView listView = (ListView) findViewById(R.id.listView);

        //region listView adapter init
        adapter = new SimpleCursorAdapter(
                this,                   // context
                R.layout.item_layout,   // layout
                null,                   // cursor (will be loaded later)
                new String[] {          // from COLUMNS
                        F2FTable.RECIPE_ID,
                        F2FTable.TITLE,
                        F2FTable.SOCIAL_RANK,
                        F2FTable.IMAGE_URI
                },
                new int[] {             // to VIEWS
                        R.id.item_recipe_id,
                        R.id.item_title,
                        R.id.item_rank,
                        R.id.item_image
                },
                0                       // flags
        );
        //endregion init
        // Set adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String recipeId = String.valueOf(
                        ((TextView) view.findViewById(R.id.item_recipe_id)).getText());
                showDetails(recipeId);
            }

        });

        initLoader(LOADER_RECIPES, queryUri.toString());
    }

    /**
     * Set toolbar according to searchIsActive value
     * @param toolbar
     */
    private void checkToolbarSearch(boolean searchState, Toolbar toolbar){
        if (!searchState) {
            toolbar.addView(searchInput);
            searchInput.requestFocus();
        } else {
            searchInput.clearFocus();
            toolbar.removeView(searchInput);
        }
    }

    /**
     * Starts new activity to show recipe details
     * @param recipeId
     */
    private void showDetails(String recipeId) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(RECIPE_ID, recipeId);
        Log.w(_TAG, recipeId);
        startActivity(intent);
    }

    //region loader

    /**
     * Initialize new loader to load {@link Cursor} data
     * @param id loader id
     * @param uri request Uri
     */
    private void initLoader(int id, String uri) {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_URI, uri);
        getLoaderManager().initLoader(id, bundle, this);
    }

    /**
     * Restart previously initialized loader to load new data
     * @param id loader id
     * @param uri new request Uri
     */
    private void restartLoader(int id, String uri) {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_URI, uri);
        getLoaderManager().restartLoader(id, bundle, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.w(MainActivity.class.getSimpleName(), "loader " + id + " created");

        Uri uri = Uri.parse(
                args != null
                        ? args.getString(QUERY_URI, F2FContentProvider.SEARCH_URI.toString())
                        : F2FContentProvider.SEARCH_URI.toString()
        );
        return new CursorLoader(this, uri, new String[]{
                F2FTable._ID,
                F2FTable.RECIPE_ID,
                F2FTable.TITLE,
                F2FTable.SOCIAL_RANK,
                F2FTable.IMAGE_URI},
                null, null, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // set new cursor after loading
        adapter.changeCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }
    //endregion

    //region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchCancelIcon = menu.findItem(R.id.search_cancel);
        return true;
    }

    /**
     * Reload data if sorting order changed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.sort_trending:
                queryUri.setSort(F2FUriHolder.Order.TRENDING);
                restartLoader(LOADER_RECIPES, queryUri.toString());
                return true;
            case R.id.sort_rating:
                queryUri.setSort(F2FUriHolder.Order.RATING);
                restartLoader(LOADER_RECIPES, queryUri.toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

}
