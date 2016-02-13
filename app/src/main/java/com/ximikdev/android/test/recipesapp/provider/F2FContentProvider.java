package com.ximikdev.android.test.recipesapp.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.ximikdev.android.test.recipesapp.database.F2FDatabaseHelper;
import com.ximikdev.android.test.recipesapp.database.F2FInsertScheduler;
import com.ximikdev.android.test.recipesapp.database.F2FTable;
import com.ximikdev.android.test.recipesapp.database.InsertDBScheduler;
import com.ximikdev.android.test.recipesapp.restframework.RESTContentProvider;
import com.ximikdev.android.test.recipesapp.restframework.StringResponseHandler;
import com.ximikdev.android.test.recipesapp.restframework.URLRequest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Foot2Fork recipes Content Provider with REST functionality.
 * API instructions could be found at
 * <a href="http://food2fork.com/about/api">http://food2fork.com/about/api</a> <br/>
 * All search requests should be made to the base search API Uri
 * <code>conext://com.ximikdev.android.test.f2f/search</code> <br/>
 * All recipe requests should be made to this Uri
 * <code>conext://com.ximikdev.android.test.f2f/get</code> <br/>
 * Parameters should be the same as for URL query: <br/>
 * <b>Search</b> <br/>
 * <i>key</i>: API Key <br/>
 * <i>q</i>: (optional) Search Query (Ingredients should be separated by commas).
 * If this is omitted top rated recipes will be returned. <br/>
 * <i>sort</i>: (optional) How the results should be sorted.
 * Top rated <i>sort=r</i>, most trending <i>sort=t</i>. <br/>
 * <i>page</i>: (optional) Used to get additional results <br/>
 * <b>Get Recipe</b> <br/>
 * <i>key</i>: API Key <br/>
 * <i>rId</i>: Id of desired recipe as returned by Search Query <br/>
 * <br/>
 * '@SuppressWarnings("ConstantConditions")' is added to avoid warnings
 * in getContext().getContentResolver() cause that is clear that
 * Content provider always has a context
 */
@SuppressWarnings("ConstantConditions")
public class F2FContentProvider extends RESTContentProvider implements
        URLRequest.OnCompleteListener, InsertDBScheduler.InsertionCompleteListener {
    public static final String _TAG = F2FContentProvider.class.getSimpleName();
    public static final String URL_API_BASE = "http://food2fork.com/api";
    public static final String DEFAULT_API_KEY = "9b149aac88e5ecbb07532ce12296b65d";
    public static final int RESULTS_PER_PAGE = 30;

    private static final int UPDATE_TIME_MS = 10 * 1000; // 2 min
    private static final String IMAGES_DIR = "/DCIM/food2fork";

    //region URI Region
    // UriMatcher case constants
    private static final int SEARCH_CASE = 1;
    private static final int GET_CASE = 2;

    // Content types
    public static final String RECIPES_TYPE = "vnd.android.cursor.dir/vnd.food2fork.recipe";
    public static final String RECIPE_ITEM_TYPE = "vnd.android.cursor.item/vnd.food2fork.recipe";

    // Uri basement
    private static final String AUTHORITY = "com.ximikdev.android.test.food2fork";
    public static final String SEARCH_PATH = "search";
    public static final String GET_PATH = "get";

    public static final Uri SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/" + SEARCH_PATH);
    public static final Uri GET_URI = Uri.parse("content://" + AUTHORITY + "/" + GET_PATH);

    // Uri Matcher
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, SEARCH_PATH, SEARCH_CASE);
        sURIMatcher.addURI(AUTHORITY, GET_PATH, GET_CASE);
    }
    //endregion

    // Main values
    SQLiteOpenHelper database;
    InsertDBScheduler insertScheduler;
    F2FJsonHandler jsonHandler;
    File imagesDir;
    F2FImagesHandler imagesHandler;
    Uri notifyUriOnComplete;

    @Override
    public boolean onCreate() {
        database = new F2FDatabaseHelper(getContext());
        insertScheduler = new F2FInsertScheduler(database, this);
        jsonHandler = new F2FJsonHandler(new StringResponseHandler());
        imagesDir = new File(Environment.getExternalStorageDirectory() + IMAGES_DIR);
        if (!imagesDir.exists()) {
            Log.i(_TAG, "images folder creation: " + imagesDir.mkdir());
        }
        imagesHandler = new F2FImagesHandler(imagesDir);
        return false;
    }


    /**
     * URL response listener
     *
     * @param url      requested URL
     * @param response generic type URL response
     */
    @Override
    public void onRequestComplete(URL url, Object response) {
        super.onRequestComplete(url, response);
        // If there is no response maybe there was bad URL. Do nothing.
        if (response == null) {
            Log.w(_TAG, "Empty response caught from: " + url);
            return;
        }
        // Retrieve query from URL
        Uri uri = F2FUriHolder.UrlToUri(
                url.toString(),
                URL_API_BASE,
                "content://" + AUTHORITY);
        F2FUri fUri = F2FUri.parse(uri);
        switch (fUri.getAction()) {
            case GET_PATH:
            case SEARCH_PATH:
                // Response from F2FJsonHandler (List<ContentValues>)
                List<ContentValues> list = (List<ContentValues>) response;
                insertScheduler.insert(list);
                notifyUriOnComplete = fUri.getUri();
                break;
            default:
                // Response from StreamResponseHandler (File)
                insertImageUriIntoDb(url, (File) response);
                break;
        }
    }

    /**
     * URL response listener helper method
     *
     * @param url      image URL
     * @param response downloaded file instance
     */
    private void insertImageUriIntoDb(URL url, File response) {
        ContentValues values = new ContentValues();
//        Log.d(_TAG, response.toURI().toString());
        values.put(F2FTable.IMAGE_URI, response.toURI().toString());
        database.getWritableDatabase().update(F2FTable.NAME, values,
                F2FTable.IMAGE_URL + "=?", new String[]{url.toString()});
        getContext().getContentResolver().notifyChange(SEARCH_URI, null);
    }

    /**
     * Insertion complete listener
     *
     * @param rowsAffected number of inserted and/or updated rows
     */
    @Override
    public void onInsertionCompete(int rowsAffected) {
        Log.i(_TAG, rowsAffected + " rows affected");
        getContext().getContentResolver().notifyChange(notifyUriOnComplete, null);
    }

    /**
     * Main REST query. All requests are handled here. Firstly it is looking for
     * data in database. All results returned via {@link Cursor}. Than if network is available
     * it is downloading newer results and storing them to database. When request is completed
     * it is sending notification to {@link Cursor}, and force it to update its data.
     *
     * @param uri           generic request Uri with query parameters, similar to REST URL.
     * @param projection    requested columns, null to return all available
     * @param selection     requested condition, can be null
     * @param selectionArgs condition arguments, null if not specified in selection by '?' symbols
     * @param sortOrder     sorting order, null if default
     * @return cursor table filled with data
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.i(_TAG, "query call: " + uri);

        //region Database query
        // Open database
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor c = null;
        // check if the caller has requested a column which does not exists
        F2FTable.checkColumns(projection);

        // Parse query and check API_KEY. For images case use original uri.
        F2FUri fUri = F2FUri.parse(uri);
        if (!fUri.hasKey()) {
            fUri = F2FUri.parse(fUri
                            + (fUri.toString().indexOf('?') == -1
                            ? "?" + F2FUri.KEY + "=" + DEFAULT_API_KEY
                            : "&" + F2FUri.KEY + "=" + DEFAULT_API_KEY)
            );
        }

        // Analyse Uri
        switch (sURIMatcher.match(uri)) {
            // If GET query select row with requested 'rId'
            case GET_CASE:
                if (fUri.hasRid()) {
                    c = db.query(
                            F2FTable.NAME,                                      //table
                            projection,                                         //columns
                            F2FTable.RECIPE_ID + " = '" + fUri.getRid() + "'",  //WHERE ...
                            null, null, null, null);
                } else {
                    throw new IllegalArgumentException("Resource id field (rId) is missing "
                            + "in GET Uri: " + uri);
                }
                break;
            // If SEARCH check for 'q' words, and return matching results. When 'q' is
            // not specified it is a special case and it has two own sorting columns.
            case SEARCH_CASE:
                if (fUri.hasQ()) {
                    c = db.query(
                            F2FTable.NAME,
                            projection,
                            F2FTable.Q + " = '" + fUri.getQ() + "'",
                            null, null, null,
                            // SORT BY ..
                            fUri.hasSort() && (fUri.getSort().equals(F2FUri.TRENDING))
                                    ? F2FTable.Q_TRENDING_POSITION
                                    : F2FTable.Q_RATING_POSITION
                    );
                } else {
                    if (fUri.getSort().equals(F2FUri.TRENDING)) {
                        c = db.query(
                                F2FTable.NAME,
                                projection,
                                F2FTable.TRENDING_POSITION + " > 0",    // WHERE ..
                                null, null, null,
                                F2FTable.TRENDING_POSITION              // SORT BY ..
                        );
                    } else {
                        c = db.query(
                                F2FTable.NAME,
                                projection,
                                F2FTable.RATING_POSITION + " > 0",    // WHERE ..
                                null, null, null,
                                F2FTable.RATING_POSITION              // SORT BY ..
                        );
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        //endregion

        //region Network update
        if (isOnline()) {
            boolean needToUpdate = false;
            // Check Cursor. If empty than we have
            // a) Bad request URL. onRequestComplete will take care if it.
            // b) Database does not contain these URL results yet. -> update
            if (c.getCount() == 0) {
                needToUpdate = true;
            } else {
                // Check F2FTable.MODIFIED and F2FTable.INGREDIENTS fields
                try {
                    // Build _id range (e.g. (2,4,5,11)) for SQLs 'IN' selection method
                    // from database data matching current request
                    StringBuilder inRange = new StringBuilder();
                    inRange.append('(');
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        inRange.append(c.getString(c.getColumnIndex(F2FTable._ID)));
                        inRange.append(',');
                    }
                    inRange.deleteCharAt(inRange.length() - 1);   //delete last comma
                    inRange.append(')');    //add closing bracket
                    String range = inRange.toString();
                    Cursor m = db.query(F2FTable.NAME,
                            new String[]{F2FTable._ID, F2FTable.MODIFIED, F2FTable.INGREDIENTS},
                            F2FTable._ID + " IN " + range,
                            null, null, null, null);
                    // Check 'modified' timestamp field for every _id in range
                    for (m.moveToFirst(); !m.isAfterLast(); m.moveToNext()) {
                        Timestamp time = Timestamp.valueOf(
                                m.getString(m.getColumnIndex(F2FTable.MODIFIED)));
                        // If data is old -> update
                        if (needToUpdate = needUpdate(time.getTime(), UPDATE_TIME_MS)) {
                            break;
                            // If it is single query (GET) check F2FTable.INGREDIENTS
                        } else if (fUri.hasRid()) try {
                            //call whatever String method. It will throw when the field is empty
                            m.getString(m.getColumnIndex(F2FTable.INGREDIENTS)).length();
                        } catch (Exception e) {
                            // If ingredients is empty -> update
                            Log.i(_TAG, "ingredients field is empty");
                            needToUpdate = true;
                            break;
                        }
                    }
                    m.close();
                } catch (Exception e) {
                    Log.e(_TAG, "Network update: " + e);
                }
            }

//            Log.d(_TAG, "need update: " + needToUpdate);

            // Update if results is older than UPDATE_TIME_MS
            if (needToUpdate) {
                try {
                    // Create matching URL
                    URL urlRequest = F2FUriHolder.uriToUrl(
                            uri.toString(),
                            "content://" + AUTHORITY,
                            URL_API_BASE
                    );
                    Log.i(_TAG, "URL request call: " + urlRequest);
                    asyncRequest(urlRequest, jsonHandler, this);
                } catch (MalformedURLException e) {
                    Log.e(_TAG, e.toString());
                }
            }

            // Check images, if missing -> update
            loadImagesIfMissing(db);
        }
        //endregion

        // make sure that potential listeners are getting notified
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    /**
     * Query method helper function. It checks difference between last data modification
     * and current time.
     *
     * @param modified     last modification time in ms since Jan, 1970
     * @param updateTimeMs update interval
     * @return
     */
    private boolean needUpdate(long modified, long updateTimeMs) {
        long offset = new Date().getTime() - modified
                - TimeZone.getDefault().getOffset(modified);
//        Log.i(_TAG, offset / 1000 + " ms");
        return offset > updateTimeMs;
    }

    /**
     * Internet access checker
     *
     * @return true if internet connection is established
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Images checker. Looking for IMAGE_URI missing fields and download data
     *
     * @param db database to search in
     */
    private void loadImagesIfMissing(SQLiteDatabase db) {
        // Select IMAGE_URL fields where IMAGE_URI is not specified
        Cursor imageUris = db.query(F2FTable.NAME,
                new String[]{F2FTable.IMAGE_URL},
                F2FTable.IMAGE_URI + " IS NULL", null, null, null, null);
        if (imageUris.getCount() > 0) {
            for (imageUris.moveToFirst(); !imageUris.isAfterLast(); imageUris.moveToNext()) {
                try {
                    URL url = new URL(imageUris.getString(0));
                    asyncRequest(url, imagesHandler, this); //download missing
                } catch (MalformedURLException e) {
                    Log.e(_TAG, e.toString());
                }
            }
        }
        imageUris.close();
    }


    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_CASE:
                return RECIPES_TYPE;
            case GET_CASE:
                return RECIPE_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented!");
    }

}
