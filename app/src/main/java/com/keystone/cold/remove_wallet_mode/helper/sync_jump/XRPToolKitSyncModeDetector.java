package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.NearAddressFilter;

public class XRPToolKitSyncModeDetector extends BaseSyncModeDetector {

    public XRPToolKitSyncModeDetector() {
        coinId = Coins.XRP.coinId();
    }

    @Override
    protected boolean isSelectOneAddress() {
        return true;
    }
}
