package com.keystone.cold.ui.fragment.main.near.model.actions.accesskey;

public class FullAccess extends KeyPermission {
    public FullAccess() {
        permissionType = "Full Access";
    }

    @Override
    public String toString() {
        return "FullAccess{" +
                "permissionType='" + permissionType + '\'' +
                '}';
    }
}
