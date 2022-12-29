package com.keystone.cold.remove_wallet_mode.helper.sync_jump;

import com.keystone.coinlib.utils.Coins;

public class FewchaWalletAddressDetector extends BaseAddressDetector {

    public FewchaWalletAddressDetector() {
        coinId = Coins.APTOS.coinId();
    }
}
