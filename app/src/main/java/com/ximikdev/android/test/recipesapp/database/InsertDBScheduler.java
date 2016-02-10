package com.ximikdev.android.test.recipesapp.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * DB synchronised insert operations
 */
public abstract class InsertDBScheduler {
    public interface InsertionCompleteListener {
        void onInsertionCompete(int rowsInserted);
    }

    private InsertionCompleteListener listener;
    private boolean listenerIsSet;
    private int rowsInserted;

    private final LinkedList<InsertTask> queue = new LinkedList<>();
    protected volatile SQLiteOpenHelper databaseHelper;

    public InsertDBScheduler(SQLiteOpenHelper database) {
        this.databaseHelper = database;
        listenerIsSet = false;
    }

    public InsertDBScheduler(SQLiteOpenHelper database, InsertionCompleteListener listener) {
        this.databaseHelper = database;
        this.listener = listener;
        listenerIsSet = listener != null;
        rowsInserted = 0;
    }

    public void insert(List<ContentValues> list) {
        for (ContentValues row : list) {
            addTask(row);
        }
    }

    void addTask(ContentValues row) {
        InsertTask task = new InsertTask();
        synchronized (queue) {
            queue.add(task);
            task.execute(row);
        }
    }

    void removeTask(InsertTask task) {
        synchronized (queue) {
            queue.remove(task);
            if (queue.isEmpty() && listenerIsSet) {
                listener.onInsertionCompete(rowsInserted);
                rowsInserted = 0;
            }
        }
    }

    private class InsertTask extends AsyncTask<ContentValues, Void, Boolean> {
        @Override
        protected Boolean doInBackground(ContentValues... params) {
            return insertValues(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) rowsInserted++;
            removeTask(this);
        }
    }

    /**
     * Custom insert method
     * @param values insert values
     * @return success
     */
    protected abstract boolean insertValues(ContentValues values);
}