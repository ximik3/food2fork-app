package com.ximikdev.android.test.recipesapp.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Food2Fork database table
 */
public class F2FTable implements BaseColumns {
    private static final String _TAG = "F2FTable";

    // JSON response table values
    public static final String RECIPES_ARR = "recipes";
    public static final String RECIPE = "recipe";
    public static final String PUBLISHER = "publisher";
    public static final String F2F_URL = "f2f_url";
    public static final String TITLE = "title";
    public static final String SOURCE_URL = "source_url";
    public static final String RECIPE_ID = "recipe_id";
    public static final String IMAGE_URL = "image_url";
    public static final String SOCIAL_RANK = "social_rank";
    public static final String PUBLISHER_URL = "publisher_url";
    public static final String INGREDIENTS = "ingredients";
    // Query field - search words separated by commas
    public static final String Q = "q";
    // Indexes to sort search results
    public static final String Q_RATING_POSITION = "q_rating_pos";
    public static final String Q_TRENDING_POSITION = "q_trending_pos";
    // Top rated and top trending numbers
    public static final String RATING_POSITION = "rating_pos";
    public static final String TRENDING_POSITION = "trending_pos";
    // Image Uri to local images cache
    public static final String IMAGE_URI = "image_uri";
    // Timestamp
    public static final String MODIFIED = "modified";
    private static final String TRIGGER = "changed";

    // Table name
    public static final String NAME = "recipes";

    // SQL query for table creation
    public static final String CREATE_TABLE = "create table " +
            NAME + " ("
            + _ID + " integer primary key autoincrement, "
            + PUBLISHER + " text, "
            + F2F_URL + " text, "
            + TITLE + " text, "
            + SOURCE_URL + " text, "
            + RECIPE_ID + " text unique, "
            + IMAGE_URL + " text, "
            + SOCIAL_RANK + " real, "
            + PUBLISHER_URL + " text, "
            + INGREDIENTS + " text, "
            + Q + " text, "
            + Q_RATING_POSITION + " integer unique, "
            + Q_TRENDING_POSITION + " integer unique, "
            + RATING_POSITION + " integer unique, "
            + TRENDING_POSITION + " integer unique, "
            + IMAGE_URI + " text, "
            + MODIFIED + " timestamp default CURRENT_TIMESTAMP" +
            ");";

    public static final String CREATE_TIMESTAMP_TRIGGER = "CREATE TRIGGER " + TRIGGER
            + " AFTER UPDATE ON " + NAME + " FOR EACH ROW"
            + " BEGIN"
            + " UPDATE " + NAME + " SET " + MODIFIED + " = CURRENT_TIMESTAMP"
            + " WHERE " + RECIPE_ID + " = old." + RECIPE_ID + "; " +
            "END;";

    /**
     * Table creation
     * @param database
     */
    public static void onCreate(SQLiteDatabase database) {
//        Log.i(_TAG, "onCreate started");
        database.execSQL("drop table if exists " + NAME);
        database.execSQL(CREATE_TABLE);
        database.execSQL(CREATE_TIMESTAMP_TRIGGER);
    }

    /**
     * Table re-creation during database version upgrade
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(F2FTable.class.getSimpleName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + NAME);
        onCreate(database);
    }

    /**
     * Full table projection
     * @return array of column names
     */
    public static String[] availableColumns() {
        return new String[]{_ID, PUBLISHER, F2F_URL, TITLE, SOURCE_URL, RECIPE_ID, IMAGE_URL,
                SOCIAL_RANK, PUBLISHER_URL, INGREDIENTS, Q, Q_RATING_POSITION,
                Q_TRENDING_POSITION, RATING_POSITION, TRENDING_POSITION, IMAGE_URI, MODIFIED};
    }

    /**
     * Check if table contains all requested columns
     * @param projection list of requested columns
     */
    public static void checkColumns(String[] projection) {
        String[] available = availableColumns();

        if (projection != null) {
            Set<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            Set<String> availableColumns = new HashSet<>(Arrays.asList(available));

            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
