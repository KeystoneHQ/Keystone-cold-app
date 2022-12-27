package com.keystone.cold.remove_wallet_mode.helper.sync_step;

import com.keystone.coinlib.utils.Coins;

public class FewchaWalletAddressDetector extends BaseAddressDetector {

    public FewchaWalletAddressDetector() {
        coinId = Coins.APTOS.coinId();
    }
}
