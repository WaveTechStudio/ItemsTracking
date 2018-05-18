package com.keeping.itemstrack.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class for a Item.
 */
@Entity(tableName = "items")
public final class Item {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @Nullable
    @ColumnInfo(name = "title")
    private final String mTitle;

    @Nullable
    @ColumnInfo(name = "description")
    private final String mDescription;

    @Nullable
    @ColumnInfo(name = "cost")
    private final String mCost;

    @Nullable
    @ColumnInfo(name = "location")
    private final String mLocation;


    @Nullable
    @ColumnInfo(name = "imagePath")
    private final String mImagePath;

    /**
     * Use this constructor to create a new completed Item.
     *
     * @param title       title of the item
     * @param description description of the item
     * @param cost        cost of the item
     * @param location    location of the item
     */
    @Ignore
    public Item(@Nullable String title, @Nullable String description, @Nullable String cost, @Nullable String location, @Nullable String imagePath) {
        this(title, description, cost, location, imagePath, UUID.randomUUID().toString());
    }

    /**
     * Use this constructor to specify a completed Item if the Item already has an id (copy of
     * another Item).
     *
     * @param title       title of the item
     * @param description description of the item
     * @param cost        cost of the item
     * @param location    location of the item
     * @param id          id of the item
     */
    public Item(@Nullable String title, @Nullable String description, @Nullable String cost, @Nullable String location, @Nullable String imagePath,
                @NonNull String id) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mCost = cost;
        mLocation = location;
        mImagePath = imagePath;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (!Strings.isNullOrEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @Nullable
    public String getCost() {
        return mCost;
    }

    @Nullable
    public String getLocation() {
        return mLocation;
    }

    @Nullable
    public String getImagePath() {
        return mImagePath;
    }


    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mTitle) &&
                Strings.isNullOrEmpty(mDescription) && Strings.isNullOrEmpty(mCost) &&
                Strings.isNullOrEmpty(mCost) &&
                Strings.isNullOrEmpty(mImagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equal(mId, item.mId) &&
                Objects.equal(mTitle, item.mTitle) &&
                Objects.equal(mDescription, item.mDescription) &&
                Objects.equal(mCost, item.mCost) &&
                Objects.equal(mLocation, item.mLocation) &&
                Objects.equal(mImagePath, item.mImagePath);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(mId, mTitle, mDescription, mCost, mLocation, mImagePath);
    }

    @Override
    public String toString() {
        return "Item with title " + mTitle;
    }
}
