package com.keeping.itemstrack.itemdetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.data.source.ItemsRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link ItemDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class ItemDetailPresenter implements ItemDetailContract.Presenter {

    private final ItemsRepository mItemsRepository;

    private final ItemDetailContract.View mItemDetailView;

    @Nullable
    private String mItemId;

    public ItemDetailPresenter(@Nullable String taskId,
                               @NonNull ItemsRepository tasksRepository,
                               @NonNull ItemDetailContract.View taskDetailView) {
        mItemId = taskId;
        mItemsRepository = checkNotNull(tasksRepository, "itemsRepository cannot be null!");
        mItemDetailView = checkNotNull(taskDetailView, "itemDetailView cannot be null!");

        mItemDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        openItem();
    }

    private void openItem() {
        if (Strings.isNullOrEmpty(mItemId)) {
            mItemDetailView.showMissingItem();
            return;
        }

        mItemDetailView.setLoadingIndicator(true);
        mItemsRepository.getItem(mItemId, new ItemsDataSource.GetItemCallback() {
            @Override
            public void onItemLoaded(Item item) {
                // The view may not be able to handle UI updates anymore
                mItemDetailView.setLoadingIndicator(false);
                if (null == item) {
                    mItemDetailView.showMissingItem();
                } else {
                    showItem(item);
                }
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                mItemDetailView.showMissingItem();
            }
        });
    }

    @Override
    public void editItem() {
        if (Strings.isNullOrEmpty(mItemId)) {
            mItemDetailView.showMissingItem();
            return;
        }
        mItemDetailView.showEditItem(mItemId);
    }

    @Override
    public void deleteItem() {
        if (Strings.isNullOrEmpty(mItemId)) {
            mItemDetailView.showMissingItem();
            return;
        }
        mItemsRepository.deleteItem(mItemId);
        mItemDetailView.showItemDeleted();
    }

    private void showItem(@NonNull Item item) {
        String title = item.getTitle();
        String description = item.getDescription();
        String cost = item.getCost();
        String loation = item.getLocation();
        String imagePath = item.getImagePath();

        if (Strings.isNullOrEmpty(title)) {
            mItemDetailView.hideTitle();
        } else {
            mItemDetailView.showTitle(title);
        }

        if (Strings.isNullOrEmpty(description)) {
            mItemDetailView.hideDescription();
        } else {
            mItemDetailView.showDescription(description);
        }

        if (Strings.isNullOrEmpty(cost)) {
            mItemDetailView.hideCost();
        } else {
            mItemDetailView.showCost(cost);
        }

        if (Strings.isNullOrEmpty(loation)) {
            mItemDetailView.hideLocation();
        } else {
            mItemDetailView.showLocation(loation);
        }
        if (Strings.isNullOrEmpty(imagePath)) {
            mItemDetailView.hideImage();
        } else {
            mItemDetailView.showImage(imagePath);
        }
    }
}
