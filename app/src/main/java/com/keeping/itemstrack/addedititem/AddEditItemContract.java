package com.keeping.itemstrack.addedititem;

import android.content.Context;

import com.keeping.itemstrack.BasePresenter;
import com.keeping.itemstrack.BaseView;

/**
 * This specifies the contract between the add/edit item view and its presenter.
 */
public interface AddEditItemContract {

    interface View extends BaseView<Presenter> {

        void showEmptyItemError();

        void showItemsList();

        void setTitle(String title);

        void setDescription(String description);

        void setCost(String cost);

        void setLocation(String location);

        void setImagePath(String imagePath);

    }

    interface Presenter extends BasePresenter {

        void saveItem(String title, String description, String cost, String location, String imagePath);

        void populateItem();

        void attachImageFromCamera(Context context);

        void attachImageFromGallery(Context context);

        boolean isDataMissing();
    }
}
