package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.helper.setup.AptosCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.ArweaveCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.BitcoinCashCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.BitcoinCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.CardanoCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.CosmosCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.Creator;
import com.keystone.cold.remove_wallet_mode.helper.setup.DashCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.EthereumCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.LitecoinCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.NearCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.RippleCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SolanaCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SubstrateCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.SuiCreator;
import com.keystone.cold.remove_wallet_mode.helper.setup.TronCreator;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;


public class SetupManager {

    public interface Callback {
        void onComplete();
    }

    public static void setup(Callback callback) {
        ExtendedPublicKeyCacheHelper.getInstance().clearCache();
        Coins.SUPPORTED_COINS
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
        if (coinId.equals(Coins.SUI.coinId())) {
            return new SuiCreator();
        } else if (coinId.equals(Coins.APTOS.coinId())) {
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
        } else if (coinId.equals(Coins.BTC_CORE_WALLET.coinId())) {
            return new BitcoinCreator(Coins.BTC_CORE_WALLET);
        } else if (coinId.equals(Coins.NEAR.coinId())) {
            return new NearCreator();
        } else if (Coins.isCosmosFamilyByCoinId(coinId)) {
            return new CosmosCreator(Coins.coinOfCoinId(coinId));
        } else if (coinId.equals(Coins.AR.coinId())) {
            return new ArweaveCreator();
        } else if (coinId.equals(Coins.LTC.coinId())) {
            return new LitecoinCreator();
        } else if (coinId.equals(Coins.BCH.coinId())) {
            return new BitcoinCashCreator();
        } else if (coinId.equals(Coins.DASH.coinId())) {
            return new DashCreator();
        } else if (coinId.equals(Coins.TRON.coinId())) {
            return new TronCreator();
        } else if (coinId.equals(Coins.XRP.coinId())) {
            return new RippleCreator();
        } else if (coinId.equals(Coins.ADA.coinId())) {
            return new CardanoCreator();
        }
        return null;
    }
}
