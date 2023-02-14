package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.helper.setup.AptosCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.BitcoinCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.CosmosCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.Creator;
import com.keystone.cold.remove_wallet_mode.helper.setup.EthereumCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.NearCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SolanaCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SubstrateCreator;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;


public class SetupManager {

    public interface Callback {
        void onComplete();
    }

    public static void setup(Callback callback) {
        ExtendedPublicKeyCacheHelper.getInstance().clearCache();
        Coins.SUPPORTED_COINS.stream()
                .filter(coin -> !coin.coinCode().equals(Coins.AR.coinCode()))
                .forEach(coin -> {
                    Creator creator = getCreator(coin);
                    if (creator != null) {
                        creator.setUp();
                    }
                });
        ExtendedPublicKeyCacheHelper.getInstance().clearCache();
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
        } else if (coinId.equals(Coins.DOT.coinId())) {
            return new SubstrateCreator(Coins.DOT);
        } else if (coinId.equals(Coins.KSM.coinId())) {
            return new SubstrateCreator(Coins.KSM);
        } else if (coinId.equals(Coins.BTC_LEGACY.coinId())) {
            return new BitcoinCreator(Coins.BTC_LEGACY);
        } else if (coinId.equals(Coins.BTC.coinId())) {
            return new BitcoinCreator(Coins.BTC);
        } else if (coinId.equals(Coins.BTC_NATIVE_SEGWIT.coinId())) {
            return new BitcoinCreator(Coins.BTC_NATIVE_SEGWIT);
        } else if (coinId.equals(Coins.NEAR.coinId())) {
            return new NearCreator();
        } else if (Coins.isCosmosFamilyByCoinId(coinId)) {
            return new CosmosCreator(Coins.coinOfCoinId(coinId));
        }
        return null;
    }
}
