package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

public class WalletMapToCoinHelper {

    public static String mapToCoinId(String walletId) {
        Wallet wallet = Wallet.getWalletById(walletId);
        switch (wallet) {
            case FEWCHA:
                return Coins.APTOS.coinId();
            case SOLFLARE:
                return Coins.SOL.coinId();
            default:
                throw new RuntimeException("unsupported wallet!!!");
        }
    }
}
