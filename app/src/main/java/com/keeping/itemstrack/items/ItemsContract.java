package com.keeping.itemstrack.items;

import android.support.annotation.NonNull;

import com.keeping.itemstrack.BasePresenter;
import com.keeping.itemstrack.BaseView;
import com.keeping.itemstrack.data.Item;

import java.util.List;

/**
 * Item's contract between the list items and the its presenter.
 */
public interface ItemsContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showItems(List<Item> items);

        void showAddItem();

        void showItemDetailsUi(String itemId);

        void showLoadingItemsError();

        void showNoItems();

        void showSuccessfullySavedMessage();

        void showSuccessfullySyncingItems();

        void showGeneralError(String message);

    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadItems(boolean forceUpdate);

        void addNewItem();

        void openItemDetails(@NonNull Item requestedTask);

        void syncItemsWithCloud();

    }
}
