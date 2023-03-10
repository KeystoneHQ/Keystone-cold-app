package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;

public class XRPToolKitSyncModeDetector extends BaseSyncModeDetector {

    public XRPToolKitSyncModeDetector() {
        coinId = Coins.XRP.coinId();
    }

    @Override
    protected boolean isSelectOneAddress() {
        return true;
    }
}
