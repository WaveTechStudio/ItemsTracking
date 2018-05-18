package com.keeping.itemstrack.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.keeping.itemstrack.data.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class ItemsRepository implements ItemsDataSource {

    private static ItemsRepository INSTANCE = null;

    private final ItemsDataSource mItemsRemoteDataSource;

    private final ItemsDataSource mItemsLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Item> mCachedItems;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private ItemsRepository(@NonNull ItemsDataSource tasksRemoteDataSource,
                            @NonNull ItemsDataSource tasksLocalDataSource) {
        mItemsRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mItemsLocalDataSource = checkNotNull(tasksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link ItemsRepository} instance
     */
    public static ItemsRepository getInstance(ItemsDataSource tasksRemoteDataSource,
                                              ItemsDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new ItemsRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(ItemsDataSource, ItemsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadItemsCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getItems(@NonNull final LoadItemsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedItems != null && !mCacheIsDirty) {
            callback.onItemsLoaded(new ArrayList<>(mCachedItems.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mItemsLocalDataSource.getItems(new LoadItemsCallback() {
                @Override
                public void onItemsLoaded(List<Item> tasks) {
                    refreshCache(tasks);
                    callback.onItemsLoaded(new ArrayList<>(mCachedItems.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveItem(@NonNull Item task) {
        checkNotNull(task);
        mItemsRemoteDataSource.saveItem(task);
        mItemsLocalDataSource.saveItem(task);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedItems == null) {
            mCachedItems = new LinkedHashMap<>();
        }
        mCachedItems.put(task.getId(), task);
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetItemCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getItem(@NonNull final String taskId, @NonNull final GetItemCallback callback) {
        checkNotNull(taskId);
        checkNotNull(callback);

        Item cachedTask = getTaskWithId(taskId);

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onItemLoaded(cachedTask);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mItemsLocalDataSource.getItem(taskId, new GetItemCallback() {
            @Override
            public void onItemLoaded(Item task) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedItems == null) {
                    mCachedItems = new LinkedHashMap<>();
                }
                mCachedItems.put(task.getId(), task);
                callback.onItemLoaded(task);
            }

            @Override
            public void onDataNotAvailable() {
                mItemsRemoteDataSource.getItem(taskId, new GetItemCallback() {
                    @Override
                    public void onItemLoaded(Item task) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedItems == null) {
                            mCachedItems = new LinkedHashMap<>();
                        }
                        mCachedItems.put(task.getId(), task);
                        callback.onItemLoaded(task);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshItems() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllItems() {
        mItemsRemoteDataSource.deleteAllItems();
        mItemsLocalDataSource.deleteAllItems();

        if (mCachedItems == null) {
            mCachedItems = new LinkedHashMap<>();
        }
        mCachedItems.clear();
    }

    @Override
    public void deleteItem(@NonNull String itemId) {
        mItemsRemoteDataSource.deleteItem(checkNotNull(itemId));
        mItemsLocalDataSource.deleteItem(checkNotNull(itemId));

        try {
            if (!itemId.isEmpty()) {
                FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
                DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference("items");
                mFirebaseDatabase.child(itemId).removeValue();
            }
        } catch (Exception e) {
        }

        mCachedItems.remove(itemId);
    }

    private void getTasksFromRemoteDataSource(@NonNull final LoadItemsCallback callback) {
        mItemsRemoteDataSource.getItems(new LoadItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> tasks) {
                refreshCache(tasks);
                refreshLocalDataSource(tasks);
                callback.onItemsLoaded(new ArrayList<>(mCachedItems.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Item> tasks) {
        if (mCachedItems == null) {
            mCachedItems = new LinkedHashMap<>();
        }
        mCachedItems.clear();
        for (Item task : tasks) {
            mCachedItems.put(task.getId(), task);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Item> tasks) {
        mItemsLocalDataSource.deleteAllItems();
        for (Item task : tasks) {
            mItemsLocalDataSource.saveItem(task);
        }
    }

    @Nullable
    private Item getTaskWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedItems == null || mCachedItems.isEmpty()) {
            return null;
        } else {
            return mCachedItems.get(id);
        }
    }
}
