package com.keystone.cold.remove_wallet_mode.ui.model;

public class ClickableItem {
    private String id;
    private String name;
    private int iconResId;

    public ClickableItem(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int iconVisibility() {
        if (iconResId == 0) {
            return 2;
        } else {
            return 0;
        }
    }
}
