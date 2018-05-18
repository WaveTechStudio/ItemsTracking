package com.keeping.itemstrack.addedititem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.keeping.itemstrack.Injection;
import com.keeping.itemstrack.R;
import com.keeping.itemstrack.util.ActivityUtils;
import com.keeping.itemstrack.util.EspressoIdlingResource;

/**
 * Displays an add or edit item screen.
 */
public class AddEditItemActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_TASK = 1;

    public static final String KEY_LOAD_DATA_FROM_REPO = "KEY_LOAD_DATA_FROM_REPO";

    private AddEditItemPresenter mAddEditTaskPresenter;

    private ActionBar mActionBar;


    /**
     * @param savedInstanceState save state of fragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddEditItemFragment addEditTaskFragment = (AddEditItemFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        String itemId = getIntent().getStringExtra(AddEditItemFragment.ARGUMENT_EDIT_ITEM_ID);

        setToolbarTitle(itemId);

        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditItemFragment.newInstance();

            if (getIntent().hasExtra(AddEditItemFragment.ARGUMENT_EDIT_ITEM_ID)) {
                Bundle bundle = new Bundle();
                bundle.putString(AddEditItemFragment.ARGUMENT_EDIT_ITEM_ID, itemId);
                addEditTaskFragment.setArguments(bundle);
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addEditTaskFragment, R.id.contentFrame);
        }

        boolean shouldLoadDataFromRepo = true;

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(KEY_LOAD_DATA_FROM_REPO);
        }

        // Create the presenter
        mAddEditTaskPresenter = new AddEditItemPresenter(
                itemId,
                Injection.provideTasksRepository(getApplicationContext()),
                addEditTaskFragment,
                shouldLoadDataFromRepo);
    }

    /**
     * @param itemId Set title of screen on the basis of id if id is null than its a new itme otherwise its being edit
     */
    private void setToolbarTitle(@Nullable String itemId) {
        if (itemId == null) {
            mActionBar.setTitle(R.string.add_item);
        } else {
            mActionBar.setTitle(R.string.edit_item);
        }
    }

    /**
     * @param outState Get the saved state of screen
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(KEY_LOAD_DATA_FROM_REPO, mAddEditTaskPresenter.isDataMissing());
        super.onSaveInstanceState(outState);
    }

    /**
     * Navigation up for back press
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Returns Idling resource count to manage loading delay
     */
    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
