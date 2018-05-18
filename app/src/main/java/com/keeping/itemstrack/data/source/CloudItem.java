package com.keeping.itemstrack.data.source;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Model class for cloud item
 */
@IgnoreExtraProperties
public class CloudItem {

    public String id;
    public String title;
    public String description;
    public String cost;
    public String location;
    public String imagePath;

    @Keep
    public CloudItem() {
    }

    @Keep
    public CloudItem(String id,
                     String title,
                     String description,
                     String cost,
                     String location,
                     String imagePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.location = location;
        this.imagePath = imagePath;
    }
}
