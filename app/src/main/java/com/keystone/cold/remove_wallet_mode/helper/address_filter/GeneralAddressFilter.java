package com.keystone.cold.remove_wallet_mode.helper.address_filter;

import com.keystone.cold.db.entity.AddressEntity;

public class GeneralAddressFilter extends AddressFilter{
    @Override
    public boolean filter(AddressEntity entity) {
        return true;
    }
}
