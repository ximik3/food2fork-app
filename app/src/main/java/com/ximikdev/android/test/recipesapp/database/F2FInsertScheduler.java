package com.ximikdev.android.test.recipesapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Custom F2F DB insert class
 */
public class F2FInsertScheduler extends InsertDBScheduler {

    public F2FInsertScheduler(SQLiteOpenHelper database, InsertionCompleteListener listener) {
        super(database, listener);
    }

    /**
     * @see InsertDBScheduler#insertValues(ContentValues)
     */
    @Override
    protected boolean insertValues(ContentValues values) {
        long affected;
        // @formatter:off
        String sortColumn = values.containsKey(F2FTable.Q)
                ? values.containsKey(F2FTable.Q_TRENDING_POSITION)
                    ? F2FTable.Q_TRENDING_POSITION
                    : F2FTable.Q_RATING_POSITION
                : values.containsKey(F2FTable.TRENDING_POSITION)
                    ? F2FTable.TRENDING_POSITION
                    : values.containsKey(F2FTable.RATING_POSITION)
                        ? F2FTable.RATING_POSITION
                        : null;
        // @formatter:on

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        synchronized (databaseHelper.getWritableDatabase()) {
            // Erase sorting position if the same found
            if (sortColumn != null) { // if it is SEARCH request
                db.execSQL("UPDATE " + F2FTable.NAME
                        + " SET " + sortColumn + " = NULL "
                        + "WHERE " + sortColumn + " = " + values.getAsString(sortColumn));
            }
            // Check if we have current recipe in our base
            Cursor c = db.query(F2FTable.NAME, null,
                    F2FTable.RECIPE_ID + " = '" + values.getAsString(F2FTable.RECIPE_ID) + "'",
                    null, null, null, null);
            if (c.getCount() > 0) {  // if we have UPDATE existing
                affected = db.update(F2FTable.NAME, values,
                        F2FTable.RECIPE_ID + " = '" + values.getAsString(F2FTable.RECIPE_ID)
                                + "'", null);
            } else {    // if we haven't INSERT new
                affected = db.insert(F2FTable.NAME, null, values);
            }
        }

        return (affected > 0);
    }
}
