package com.keystone.cold.remove_wallet_mode.helper.address_filter;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AddressEntity;

public class SolanaAddressFilter extends AddressFilter {
    @Override
    public boolean filter(AddressEntity entity) {
        String code = Utilities.getCurrentSolAccount(context);
        SOLAccount account = SOLAccount.ofCode(code);
        return isCurrentSOLAccountAddress(account, entity);
    }

    private boolean isCurrentSOLAccountAddress(SOLAccount account, AddressEntity addressEntity) {
        return account.isChildrenPath(addressEntity.getPath());
    }
}
