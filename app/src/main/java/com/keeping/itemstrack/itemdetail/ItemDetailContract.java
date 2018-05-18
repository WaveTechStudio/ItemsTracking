package com.keeping.itemstrack.itemdetail;

import com.keeping.itemstrack.BasePresenter;
import com.keeping.itemstrack.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface ItemDetailContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showMissingItem();

        void hideTitle();

        void showTitle(String title);

        void hideDescription();

        void showDescription(String description);

        void showCost(String cost);

        void hideCost();

        void showLocation(String location);

        void hideLocation();

        void showImage(String imagePath);

        void hideImage();

        void showEditItem(String itemId);

        void showItemDeleted();

    }

    interface Presenter extends BasePresenter {

        void editItem();

        void deleteItem();

    }
}
