package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.NearAddressFilter;

public class SenderWalletSyncModeDetector extends BaseSyncModeDetector {

    public SenderWalletSyncModeDetector() {
        coinId = Coins.NEAR.coinId();
    }

    @Override
    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        return new NearAddressFilter().filter(addressEntity);
    }

    @Override
    protected boolean isSelectOneAddress() {
        return true;
    }
}
