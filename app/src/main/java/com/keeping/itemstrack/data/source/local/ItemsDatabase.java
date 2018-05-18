package com.keeping.itemstrack.data.source.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.keeping.itemstrack.data.Item;

/**
 * The Room Database that contains the Task table.
 */
@Database(entities = {Item.class}, version = 1, exportSchema = false)
public abstract class ItemsDatabase extends RoomDatabase {

    private static ItemsDatabase INSTANCE;

    public abstract ItemsDao itemDao();

    private static final Object sLock = new Object();

    public static ItemsDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        ItemsDatabase.class, "Items.db")
                        .build();
            }
            return INSTANCE;
        }
    }

}
