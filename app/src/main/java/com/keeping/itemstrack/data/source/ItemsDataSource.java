package com.keeping.itemstrack.data.source;

import android.support.annotation.NonNull;

import com.keeping.itemstrack.data.Item;

import java.util.List;

/**
 * Main entry point for accessing tasks data.
 * <p>
 * For simplicity, only getItems() and getItem() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new task is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface ItemsDataSource {

    interface LoadItemsCallback {

        void onItemsLoaded(List<Item> items);

        void onDataNotAvailable();
    }

    interface GetItemCallback {

        void onItemLoaded(Item item);

        void onDataNotAvailable();
    }

    void getItems(@NonNull LoadItemsCallback callback);

    void getItem(@NonNull String itemId, @NonNull GetItemCallback callback);

    void saveItem(@NonNull Item item);

    void refreshItems();

    void deleteAllItems();

    void deleteItem(@NonNull String itemId);
}
