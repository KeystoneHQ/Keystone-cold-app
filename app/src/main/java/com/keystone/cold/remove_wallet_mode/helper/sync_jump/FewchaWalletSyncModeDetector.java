package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;

public class FewchaWalletSyncModeDetector extends BaseSyncModeDetector {

    public FewchaWalletSyncModeDetector() {
        coinId = Coins.APTOS.coinId();
    }
}
