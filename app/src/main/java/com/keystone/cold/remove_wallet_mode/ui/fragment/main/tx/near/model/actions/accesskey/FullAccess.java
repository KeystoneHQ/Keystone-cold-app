package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey;

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
