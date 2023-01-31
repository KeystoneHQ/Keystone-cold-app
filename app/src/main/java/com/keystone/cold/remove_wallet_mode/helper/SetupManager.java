package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.helper.setup.AptosCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.BaseCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.Creator;
import com.keystone.cold.remove_wallet_mode.helper.setup.EthereumCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SolanaCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SubstrateCreator;


public class SetupManager {

    public interface Callback {
        void onComplete();
    }

    public static void setup(Callback callback) {
        BaseCreator.clearCache();
        Coins.SUPPORTED_COINS.stream()
                .filter(coin -> !coin.coinCode().equals(Coins.AR.coinCode()))
                .forEach(coin -> {
                    Creator creator = getCreator(coin);
                    if (creator != null) {
                        creator.setUp();
                    }
                });
        BaseCreator.clearCache();
        if (callback != null) callback.onComplete();
    }

    //todo:  Need to add other coin
    private static Creator getCreator(Coins.Coin coin) {
        String coinId = coin.coinId();
        if (coinId.equals(Coins.APTOS.coinId())) {
            return new AptosCreator();
        } else if (coinId.equals(Coins.ETH.coinId())) {
            return new EthereumCreator();
        } else if (coinId.equals(Coins.SOL.coinId())) {
            return new SolanaCreator();
        } else if(coinId.equals(Coins.DOT.coinId())) {
            return new SubstrateCreator(Coins.DOT);
        } else if(coinId.equals(Coins.KSM.coinId())) {
            return new SubstrateCreator(Coins.KSM);
        }
        return null;
    }
}
