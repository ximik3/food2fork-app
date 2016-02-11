package com.ximikdev.android.test.recipesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Activity started from MainActivity to show Recipe details
 */
public class DetailsActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG = "com.ximikdev.android.test.recipesapp.FRAGMENT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // receive recipe_id from main activity
        Intent intent = getIntent();
        String recipe_id = intent.getStringExtra(MainActivity.RECIPE_ID);


        // prepare to send
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.RECIPE_ID, recipe_id);

        // new fragment for current recipe_id
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (manager.findFragmentByTag(FRAGMENT_TAG) == null) {
            transaction.add(R.id.frame_layout,
                    DetailsActivityFragment.newInstance(bundle),
                    FRAGMENT_TAG
            );
        }
        transaction.commit();

        //region toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion
    }
}
