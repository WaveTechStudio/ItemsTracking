package com.keeping.itemstrack;

import android.content.Context;
import android.support.annotation.NonNull;

import com.keeping.itemstrack.data.FakeItemsRemoteDataSource;
import com.keeping.itemstrack.data.source.ItemsDataSource;
import com.keeping.itemstrack.data.source.ItemsRepository;
import com.keeping.itemstrack.data.source.local.ItemsDatabase;
import com.keeping.itemstrack.data.source.local.ItemsLocalDataSource;
import com.keeping.itemstrack.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link ItemsDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static ItemsRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        ItemsDatabase database = ItemsDatabase.getInstance(context);
        return ItemsRepository.getInstance(FakeItemsRemoteDataSource.getInstance(),
                ItemsLocalDataSource.getInstance(new AppExecutors(),
                        database.itemDao()));
    }
}
