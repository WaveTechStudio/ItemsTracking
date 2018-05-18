package com.keeping.itemstrack.itemdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.keeping.itemstrack.R;
import com.keeping.itemstrack.addedititem.AddEditItemActivity;
import com.keeping.itemstrack.addedititem.AddEditItemFragment;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the task detail screen.
 */
public class ItemDetailFragment extends Fragment implements ItemDetailContract.View {

    @NonNull
    private static final String ARGUMENT_TASK_ID = "ITEM_ID";

    @NonNull
    private static final int REQUEST_EDIT_TASK = 1;

    private ItemDetailContract.Presenter mPresenter;

    private TextView mDetailTitle;
    private TextView mDetailDescription;
    private TextView mDetailCost;
    private TextView mDetailLocation;
    private ImageView item_detail_image;

    public static ItemDetailFragment newInstance(@Nullable String itemId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TASK_ID, itemId);
        ItemDetailFragment fragment = new ItemDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.itemdetail_fragment, container, false);
        setHasOptionsMenu(true);
        mDetailTitle = (TextView) root.findViewById(R.id.task_detail_title);
        mDetailDescription = (TextView) root.findViewById(R.id.task_detail_description);
        mDetailCost = (TextView) root.findViewById(R.id.task_detail_cost);
        mDetailLocation = (TextView) root.findViewById(R.id.task_detail_location);
        item_detail_image = (ImageView) root.findViewById(R.id.item_detail_image);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.editItem();
            }
        });
        return root;
    }

    @Override
    public void setPresenter(@NonNull ItemDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mPresenter.deleteItem();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.itemdetail_fragment_menu, menu);
    }

    /**
     * @param active Show loading indicator is active or inactive
     */
    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));
        }
    }

    /**
     * @param itemId Show to edit item from the given item id
     */
    @Override
    public void showEditItem(@NonNull String itemId) {
        Intent intent = new Intent(getContext(), AddEditItemActivity.class);
        intent.putExtra(AddEditItemFragment.ARGUMENT_EDIT_ITEM_ID, itemId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void showItemDeleted() {
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
            }
        }
    }


    /**
     * Show no data found error
     */
    @Override
    public void showMissingItem() {
        mDetailTitle.setText("");
        mDetailDescription.setText(getString(R.string.no_data));
    }


    /**
     * @param title Show item's title
     */
    @Override
    public void showTitle(@NonNull String title) {
        mDetailTitle.setVisibility(View.VISIBLE);
        mDetailTitle.setText(title);
    }

    /**
     * Hide item's title field
     */
    @Override
    public void hideTitle() {
        mDetailTitle.setVisibility(View.GONE);
    }


    /**
     * @param description Show item's description
     */
    @Override
    public void showDescription(@NonNull String description) {
        mDetailDescription.setVisibility(View.VISIBLE);
        mDetailDescription.setText(description);
    }

    @Override
    public void hideDescription() {
        mDetailDescription.setVisibility(View.GONE);
    }


    /**
     * @param cost Show item's cost
     */
    @Override
    public void showCost(String cost) {
        mDetailCost.setVisibility(View.VISIBLE);
        mDetailCost.setText(cost + " $");
    }


    /**
     * Hide item's cost field
     */
    @Override
    public void hideCost() {
        mDetailLocation.setVisibility(View.GONE);
    }


    /**
     * @param location Show item location
     */
    @Override
    public void showLocation(String location) {
        mDetailLocation.setVisibility(View.VISIBLE);
        mDetailLocation.setText(location);

    }

    /**
     * Hide item's location field
     */
    @Override
    public void hideLocation() {
        mDetailLocation.setVisibility(View.GONE);
    }


    /**
     * @param imagePath Show image from this path
     */
    @Override
    public void showImage(String imagePath) {
        if (!imagePath.isEmpty() && getActivity() != null) {
            Glide.with(getActivity())
                    .load(imagePath)
                    .into(item_detail_image);
        }
    }

    /**
     * Hide item image bucause image url is not available
     */
    @Override
    public void hideImage() {
        item_detail_image.setVisibility(View.GONE);
    }
}
