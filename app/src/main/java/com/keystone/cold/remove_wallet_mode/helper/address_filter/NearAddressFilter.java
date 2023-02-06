package com.keystone.cold.remove_wallet_mode.helper.address_filter;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AddressEntity;

public class NearAddressFilter extends AddressFilter {
    @Override
    public boolean filter(AddressEntity entity) {
        String code = Utilities.getCurrentNearAccount(context);
        NEARAccount account = NEARAccount.ofCode(code);
        return isCurrentNearAccountAddress(account, entity);
    }

    private boolean isCurrentNearAccountAddress(NEARAccount account, AddressEntity addressEntity) {
        return account.isChildrenPath(addressEntity.getPath());
    }
}
