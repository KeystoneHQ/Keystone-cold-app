package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;

public class PetraWalletSyncModeDetector extends BaseSyncModeDetector {

    public PetraWalletSyncModeDetector() {
        coinId = Coins.APTOS.coinId();
    }
}
