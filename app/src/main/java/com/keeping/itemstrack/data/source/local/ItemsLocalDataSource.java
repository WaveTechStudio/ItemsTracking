package com.keeping.itemstrack.data.source.local;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.keeping.itemstrack.data.Item;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.util.AppExecutors;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Concrete implementation of a data source as a db.
 */
public class ItemsLocalDataSource implements ItemsDataSource {

    private static volatile ItemsLocalDataSource INSTANCE;

    private ItemsDao mItemsDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private ItemsLocalDataSource(@NonNull AppExecutors appExecutors,
                                 @NonNull ItemsDao tasksDao) {
        mAppExecutors = appExecutors;
        mItemsDao = tasksDao;
    }

    public static ItemsLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                   @NonNull ItemsDao tasksDao) {
        if (INSTANCE == null) {
            synchronized (ItemsLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ItemsLocalDataSource(appExecutors, tasksDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadItemsCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getItems(@NonNull final LoadItemsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Item> tasks = mItemsDao.getItems();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (tasks.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onItemsLoaded(tasks);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetItemCallback#onDataNotAvailable()} is fired if the {@link Item} isn't
     * found.
     */
    @Override
    public void getItem(@NonNull final String taskId, @NonNull final GetItemCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Item task = mItemsDao.getItemById(taskId);

                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (task != null) {
                            callback.onItemLoaded(task);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveItem(@NonNull final Item task) {
        checkNotNull(task);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mItemsDao.insertItem(task);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void refreshItems() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllItems() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mItemsDao.deleteItems();
            }
        };
        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void deleteItem(@NonNull final String taskId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mItemsDao.deleteItemById(taskId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
