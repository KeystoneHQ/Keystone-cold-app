package com.keystone.cold.remove_wallet_mode.helper.address_filter;

import android.content.Context;

import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;

public abstract class AddressFilter {
    protected Context context = MainApplication.getApplication();

    public abstract boolean filter(AddressEntity entity);
}
