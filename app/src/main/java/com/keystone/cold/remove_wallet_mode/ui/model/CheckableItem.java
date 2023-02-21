package com.keystone.cold.remove_wallet_mode.ui.model;

public class CheckableItem {
    private String id;
    private String name;
    private int iconResId;
    private String description;
    private boolean checked;

    public CheckableItem(String id, String name, int iconResId, String description) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.description = description;
        this.checked = false;
    }

    public CheckableItem(String id, String name, int iconResId, String description, boolean checked) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.description = description;
        this.checked = checked;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int iconVisibility() {
        if (iconResId == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "CheckableItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", iconResId=" + iconResId +
                ", description='" + description + '\'' +
                ", checked=" + checked +
                '}';
    }
}
