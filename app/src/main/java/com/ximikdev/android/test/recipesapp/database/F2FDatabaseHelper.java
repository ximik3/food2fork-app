package com.ximikdev.android.test.recipesapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Foot2Fork database manager
 */
public class F2FDatabaseHelper extends SQLiteOpenHelper {
    private static final String _TAG = F2FDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "food2fork.db";
    private static final int DATABASE_VERSION = 1;


    public F2FDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Tables creation during database creation process
     *
     * @param db database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        F2FTable.onCreate(db);
    }

    /**
     * Called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        F2FTable.onUpgrade(db, oldVersion, newVersion);
    }
}
