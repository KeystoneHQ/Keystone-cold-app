package com.keystone.cold.remove_wallet_mode.helper.address_filter;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AddressEntity;

public class EthereumAddressFilter extends AddressFilter {

    @Override
    public boolean filter(AddressEntity entity) {
        String code = Utilities.getCurrentEthAccount(context);
        ETHAccount account = ETHAccount.ofCode(code);
        return isCurrentETHAccountAddress(account, entity);
    }

    private boolean isCurrentETHAccountAddress(ETHAccount account, AddressEntity addressEntity) {
        return account.isChildrenPath(addressEntity.getPath());
    }
}
