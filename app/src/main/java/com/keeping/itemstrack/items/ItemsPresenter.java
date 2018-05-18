package com.keeping.itemstrack.items;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keeping.itemstrack.addedititem.AddEditItemActivity;
import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.CloudItem;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.data.source.ItemsRepository;
import com.keeping.itemstrack.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link ItemsFragment}), retrieves the data and updates the
 * UI as required.
 */
public class ItemsPresenter implements ItemsContract.Presenter {

    private final ItemsRepository mItemsRepository;

    private final ItemsContract.View mItemsView;

    private boolean mFirstLoad = true;

    public ItemsPresenter(@NonNull ItemsRepository itemsRepository, @NonNull ItemsContract.View itemsView) {
        mItemsRepository = checkNotNull(itemsRepository, "itemsRepository cannot be null");
        mItemsView = checkNotNull(itemsView, "itemsView cannot be null!");

        mItemsView.setPresenter(this);
    }

    /**
     * Presenter's start method with load items
     */
    @Override
    public void start() {
        loadItems(false);
    }

    /**
     * If a item was successfully added, show snackbar
     */
    @Override
    public void result(int requestCode, int resultCode) {
        if (AddEditItemActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mItemsView.showSuccessfullySavedMessage();
        }
    }

    /**
     * @param forceUpdate Load item with either force update true or false
     */
    @Override
    public void loadItems(boolean forceUpdate) {
        loadItems(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link ItemsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadItems(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mItemsView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mItemsRepository.refreshItems();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mItemsRepository.getItems(new ItemsDataSource.LoadItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> items) {
                List<Item> itemsToShow = new ArrayList<Item>();
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                for (Item item : items) {
                    itemsToShow.add(item);
                }
                if (showLoadingUI) {
                    mItemsView.setLoadingIndicator(false);
                }

                processItems(itemsToShow);
            }

            @Override
            public void onDataNotAvailable() {
                mItemsView.showLoadingItemsError();
            }
        });
    }

    /**
     * @param items Process items, Check if data is empty otherwise show items
     */
    private void processItems(List<Item> items) {
        if (items.isEmpty()) {
            processEmptyItems();
        } else {
            mItemsView.showItems(items);
        }
    }

    /**
     * Data is empty show alert
     */
    private void processEmptyItems() {
        mItemsView.showNoItems();
    }


    /**
     * Show and new item
     */
    @Override
    public void addNewItem() {
        mItemsView.showAddItem();
    }

    /**
     * @param requestedItem Open requested item details
     */
    @Override
    public void openItemDetails(@NonNull Item requestedItem) {
        checkNotNull(requestedItem, "RequestedItem cannot be null!");
        mItemsView.showItemDetailsUi(requestedItem.getId());
    }

    /*
     * Push local items to Firebase cloud and fetch all items from cloud
     */
    @Override
    public void syncItemsWithCloud() {
        mItemsView.setLoadingIndicator(true);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference("items");

        mItemsRepository.getItems(new ItemsDataSource.LoadItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> items) {
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }
                if (!items.isEmpty()) {
                    for (Item item : items) {
                        mFirebaseDatabase.child(item.getId()).setValue(item);
                    }
                }
            }

            @Override
            public void onDataNotAvailable() {
                mItemsView.showLoadingItemsError();
            }
        });
        FirebaseDatabase.getInstance().getReference("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    CloudItem cloudItem = childSnapshot.getValue(CloudItem.class);
                    mItemsRepository.saveItem(new Item(cloudItem.title, cloudItem.description, cloudItem.cost,
                            cloudItem.location, cloudItem.imagePath, cloudItem.id));
                }
                loadItems(true);
                mItemsView.showSuccessfullySyncingItems();
                mItemsView.setLoadingIndicator(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mItemsView.showGeneralError(databaseError.getMessage());

            }
        });
    }
}
