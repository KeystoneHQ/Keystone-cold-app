package com.keystone.cold.ui.fragment.main;

import com.keystone.cold.db.entity.AddressEntity;

public interface AddressSyncCallback {
    void onClick(AddressEntity addr, int position);
}
