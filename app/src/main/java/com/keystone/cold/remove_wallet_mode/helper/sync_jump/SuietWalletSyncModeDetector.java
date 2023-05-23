package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;

public class SuietWalletSyncModeDetector extends BaseSyncModeDetector {

    public SuietWalletSyncModeDetector() {
        coinId = Coins.SUI.coinId();
    }
}
