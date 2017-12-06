package com.messedcode.openshoppinglist.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ShoppingListItem {

    @Exclude
    public String key;

    public String createdBy;
    public String name;
    public String description;
    public int price;
    public boolean urgent;
    public Status status;
    public long createdAt;

    @SuppressWarnings("unused")
    public ShoppingListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
    }

    public ShoppingListItem(String createdBy, String name, String description, int price, boolean urgent) {
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.price = price;
        this.urgent = urgent;
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public static ShoppingListItem fromSnapshot(DataSnapshot snapshot) {
        ShoppingListItem item = snapshot.getValue(ShoppingListItem.class);
        item.key = snapshot.getKey();

        return item;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ShoppingListItem && key.equals(((ShoppingListItem) obj).key);
    }

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "key='" + key + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", urgent=" + urgent +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    public void archive() {
        status = Status.ARCHIVED;
    }

    public void delete() {
        status = Status.DELETED;
    }

    @Exclude
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, ARCHIVED, DELETED
    }

}
