package com.keeping.itemstrack.addedititem;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.keeping.itemstrack.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditItemFragment extends Fragment implements AddEditItemContract.View {

    public static final String ARGUMENT_EDIT_ITEM_ID = "EDIT_ITEM_ID";

    private AddEditItemContract.Presenter mPresenter;

    private TextView mTitle;

    private TextView mDescription;

    private TextView mCost;

    private TextView mLocation;

    private ImageView mImage;

    private RelativeLayout add_image_container;

    private static String mImagePath = "";

    public static AddEditItemFragment newInstance() {
        return new AddEditItemFragment();
    }

    /*
    Required empty public constructor
     */
    public AddEditItemFragment() {
    }


    /**
     * Start the presenter in resume callback
     */
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    /**
     * @param presenter Assign the presenter to this screen
     */
    @Override
    public void setPresenter(@NonNull AddEditItemContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.additem_fragment, container, false);
        setHasOptionsMenu(true);
        mTitle = (TextView) root.findViewById(R.id.add_item_title);
        mDescription = (TextView) root.findViewById(R.id.add_item_description);
        mCost = (TextView) root.findViewById(R.id.add_item_cost);
        mLocation = (TextView) root.findViewById(R.id.add_item_location);
        mImage = (ImageView) root.findViewById(R.id.add_item_image);
        add_image_container = (RelativeLayout) root.findViewById(R.id.add_image_container);
        add_image_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageOption();
            }
        });

        return root;
    }

    /**
     * Show empty error
     */
    @Override
    public void showEmptyItemError() {
        Snackbar.make(mTitle, getString(R.string.empty_item_message), Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show items list
     */
    @Override
    public void showItemsList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }


    /**
     * @param title Set the item's title
     */
    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }


    /**
     * @param description Set the item's description
     */
    @Override
    public void setDescription(String description) {
        mDescription.setText(description);
    }


    /**
     * @param cost Set the item's location
     */
    @Override
    public void setCost(String cost) {
        mCost.setText(cost);
    }


    /**
     * @param location Set the item's location
     */
    @Override
    public void setLocation(String location) {
        mLocation.setText(location);
    }


    /**
     * @param imagePath Set the item's  imagePath
     */
    @Override
    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
        if (!imagePath.isEmpty() && getActivity() != null) {
            Glide.with(getActivity())
                    .load(mImagePath)
                    .into(mImage);
        }
    }

    /**
     * @param item Menu item for attach image and save image
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_attach_file:
                selectImageOption();
                return true;
            case R.id.menu_add_item:
                mPresenter.saveItem(mTitle.getText().toString(), mDescription.getText().toString(), mCost.getText().toString(), mLocation.getText().toString(), mImagePath);
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.additem_fragment_menu, menu);
    }


    /**
     * Select image capture source either Camera or Gallery
     */
    private void selectImageOption() {
        AppCompatDialog customDialog;
        try {
            customDialog = new AppCompatDialog(getActivity());
            customDialog.setContentView(R.layout.dialog_image_selection);

            final Button select_camera = (Button) customDialog.findViewById(R.id.select_camera);
            final Button select_gallery = (Button) customDialog.findViewById(R.id.select_gallery);


            select_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.attachImageFromCamera(getActivity());
                    customDialog.cancel();
                }
            });
            select_gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.attachImageFromGallery(getActivity());
                    customDialog.cancel();
                }
            });
            customDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
