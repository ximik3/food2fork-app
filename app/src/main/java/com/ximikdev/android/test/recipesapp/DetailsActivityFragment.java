package com.ximikdev.android.test.recipesapp;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ximikdev.android.test.recipesapp.database.F2FTable;
import com.ximikdev.android.test.recipesapp.provider.F2FContentProvider;
import com.ximikdev.android.test.recipesapp.provider.F2FUri;
import com.ximikdev.android.test.recipesapp.provider.F2FUriHolder;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a detailed recipe description.
 */
public class DetailsActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String _TAG = DetailsActivityFragment.class.getSimpleName();

    private String recipe_id;
    private F2FUriHolder queryUri;

    private ImageView image;
    private TextView title;
    private TextView publisher;
    private TextView rating;
    private TextView ingredients;
    private TextView sourceUrl;

    public DetailsActivityFragment() {
    }

    public static DetailsActivityFragment newInstance(Bundle bundle) {
        DetailsActivityFragment fragment = new DetailsActivityFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) { savedInstanceState = getArguments(); }
        if (savedInstanceState != null) {
            recipe_id = savedInstanceState.getString(MainActivity.RECIPE_ID);
        }
        queryUri = new F2FUriHolder(F2FUri.parse(F2FContentProvider.GET_URI));
        queryUri.setKey(F2FContentProvider.DEFAULT_API_KEY);
        queryUri.setRid(recipe_id);

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        image = ((ImageView) view.findViewById(R.id.details_image));
        title = ((TextView) view.findViewById(R.id.details_title));
        publisher = ((TextView) view.findViewById(R.id.details_publisher));
        rating = ((TextView) view.findViewById(R.id.details_rating));
        ingredients = ((TextView) view.findViewById(R.id.details_ingredients));
        sourceUrl = ((TextView) view.findViewById(R.id.details_source_url));
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MainActivity.RECIPE_ID, recipe_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(_TAG, "Loader " + id + " started");
        return new CursorLoader(getContext(), queryUri.toUri(), new String[]{
                F2FTable._ID,
                F2FTable.IMAGE_URI,
                F2FTable.TITLE,
                F2FTable.SOCIAL_RANK,
                F2FTable.PUBLISHER,
                F2FTable.INGREDIENTS,
                F2FTable.SOURCE_URL},
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(_TAG, data != null ? "cursor ok" : "null cursor");
        if (data != null) {
            ArrayList<String> sData = new ArrayList<>();
            data.moveToFirst(); // set starting position to 0
            for (int i = 0; i < data.getColumnCount(); i++) {
                try {
                    // Copy available data
                    sData.add(i, data.getString(i).concat(""));
                } catch (Exception e) {
                    sData.add(i, " n/a");
                    Log.e(_TAG, "cursor copy: " + e.toString());
                }
            }

            Log.i(F2FContentProvider._TAG, sData.get(data.getColumnIndex(F2FTable.IMAGE_URI)));
            image.setImageURI(Uri.parse(sData.get(data.getColumnIndex(F2FTable.IMAGE_URI))));

            title.setText(sData.get(data.getColumnIndex(F2FTable.TITLE)));

            rating.setText(R.string.details_rating);
            rating.append(" ");
            rating.append(sData.get(data.getColumnIndex(F2FTable.SOCIAL_RANK)));

            publisher.setText(R.string.details_publisher);
            publisher.append(" ");
            publisher.append(sData.get(data.getColumnIndex(F2FTable.PUBLISHER)));

            ingredients.setText(R.string.details_ingredients);
            ingredients.append("\n");
            ingredients.append(sData.get(data.getColumnIndex(F2FTable.INGREDIENTS)));

            sourceUrl.setText(R.string.details_source_url);
            sourceUrl.append(" ");
            sourceUrl.append(sData.get(data.getColumnIndex(F2FTable.SOURCE_URL)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
