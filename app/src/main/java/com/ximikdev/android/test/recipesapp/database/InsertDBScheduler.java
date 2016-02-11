package com.ximikdev.android.test.recipesapp.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.List;

/**
 * DB synchronised insert operations
 */
public abstract class InsertDBScheduler {
    /**
     * All insert operations complete listener
     */
    public interface InsertionCompleteListener {
        void onInsertionCompete(int rowsInserted);
    }

    private InsertionCompleteListener listener;
    private boolean listenerIsSet;
    private int rowsInserted;

    private final LinkedList<InsertTask> queue = new LinkedList<>();
    protected volatile SQLiteOpenHelper databaseHelper;

    /**
     * Creates an instance of {@link InsertDBScheduler}
     *
     * @param database where to insert values
     */
    public InsertDBScheduler(SQLiteOpenHelper database) {
        this.databaseHelper = database;
        listenerIsSet = false;
    }

    /**
     * Creates an instance of {@link InsertDBScheduler}
     *
     * @param database where to insert values
     * @param listener completion listener
     */
    public InsertDBScheduler(SQLiteOpenHelper database, InsertionCompleteListener listener) {
        this.databaseHelper = database;
        this.listener = listener;
        listenerIsSet = listener != null;
        rowsInserted = 0;
    }

    /**
     * Insert list of {@link ContentValues} into database
     *
     * @param list
     */
    public void insert(List<ContentValues> list) {
        for (ContentValues row : list) {
            addTask(row);
        }
    }

    /**
     * Adds single task to queue
     *
     * @param row
     */
    void addTask(ContentValues row) {
        InsertTask task = new InsertTask();
        synchronized (queue) {
            queue.add(task);
            task.execute(row);
        }
    }

    /**
     * Remove task from queue and notify if queue is empty
     *
     * @param task
     */
    void removeTask(InsertTask task) {
        synchronized (queue) {
            queue.remove(task);
            if (queue.isEmpty() && listenerIsSet) {
                listener.onInsertionCompete(rowsInserted);
                rowsInserted = 0;
            }
        }
    }

    /**
     * Background task which inserts values
     */
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
     * Abstract insert method. Must be implemented by class which inherits this class.
     *
     * @param values insert values
     * @return true is success or false
     */
    protected abstract boolean insertValues(ContentValues values);
}