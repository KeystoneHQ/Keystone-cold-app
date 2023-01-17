package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.SolanaAddressFilter;

public class SolflareWalletSyncModeDetector extends BaseSyncModeDetector {

    public SolflareWalletSyncModeDetector() {
        coinId = Coins.SOL.coinId();
    }

    @Override
    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        return new SolanaAddressFilter().filter(addressEntity);
    }
}
