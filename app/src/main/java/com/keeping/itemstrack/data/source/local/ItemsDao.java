package com.keeping.itemstrack.data.source.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.keeping.itemstrack.data.Item;

import java.util.List;

/**
 * Data Access Object for the items table.
 */
@Dao
public interface ItemsDao {

    /**
     * Select all items from the items table.
     *
     * @return all items.
     */
    @Query("SELECT * FROM items")
    List<Item> getItems();

    /**
     * Select a item by id.
     *
     * @param itemId the item id.
     * @return the item with itemId.
     */
    @Query("SELECT * FROM items WHERE entryid = :itemId")
    Item getItemById(String itemId);

    /**
     * Insert a item in the database. If the item already exists, replace it.
     *
     * @param item the item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(Item item);

    /**
     * Update a item.
     *
     * @param item item to be updated
     * @return the number of items updated. This should always be 1.
     */
    @Update
    int updateItem(Item item);


    /**
     * Delete a item by id.
     *
     * @return the number of items deleted. This should always be 1.
     */
    @Query("DELETE FROM items WHERE entryid = :itemId")
    int deleteItemById(String itemId);

    /**
     * Delete all items.
     */
    @Query("DELETE FROM items")
    void deleteItems();

}
