package com.keeping.itemstrack.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.collect.Lists;
import com.keeping.itemstrack.data.source.ItemsDataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeItemsRemoteDataSource implements ItemsDataSource {

    private static FakeItemsRemoteDataSource INSTANCE;

    private static final Map<String, Item> TASKS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeItemsRemoteDataSource() {
    }

    public static FakeItemsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeItemsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getItems(@NonNull LoadItemsCallback callback) {
        callback.onItemsLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
    }

    @Override
    public void getItem(@NonNull String taskId, @NonNull GetItemCallback callback) {
        Item task = TASKS_SERVICE_DATA.get(taskId);
        callback.onItemLoaded(task);
    }

    @Override
    public void saveItem(@NonNull Item task) {
        TASKS_SERVICE_DATA.put(task.getId(), task);
    }


    public void refreshItems() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteItem(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }

    @Override
    public void deleteAllItems() {
        TASKS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addTasks(Item... items) {
        for (Item task : items) {
            TASKS_SERVICE_DATA.put(task.getId(), task);
        }
    }
}
