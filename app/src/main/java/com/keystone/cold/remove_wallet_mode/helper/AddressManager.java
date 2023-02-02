package com.keystone.cold.remove_wallet_mode.helper;


import static com.keystone.cold.MainApplication.getApplication;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.Utilities;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AptosAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinLegacyAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNativeSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.BitcoinNestedSegwitAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.EthereumAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SubstrateAddressGenerator;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;


public class AddressManager {

    public static void addAddress(final String coinId, final int count, final MutableLiveData<Boolean> status) {
        AddressGenerator addressGenerator = getAddressGenerator(coinId);
        if (addressGenerator == null) {
            status.postValue(false);
            return;
        }
        addressGenerator.generateAddress(count, new AddressGenerator.StatusCallBack() {
            @Override
            public void onSuccess() {
                status.postValue(true);
            }

            @Override
            public void onFail() {
                status.postValue(false);

            }
        });
    }

    private static AddressGenerator getAddressGenerator(String coinId) {
        if (Coins.BTC_LEGACY.coinId().equals(coinId)) {
            return new BitcoinLegacyAddressGenerator();
        }
        if (Coins.BTC.coinId().equals(coinId)) {
            return new BitcoinNestedSegwitAddressGenerator();
        }
        if (Coins.BTC_NATIVE_SEGWIT.coinId().equals(coinId)) {
            return new BitcoinNativeSegwitAddressGenerator();
        }
        if (Coins.ETH.coinId().equals(coinId)) {
            String code = Utilities.getCurrentEthAccount(getApplication());
            return new EthereumAddressGenerator(code);
        }
        if (Coins.APTOS.coinId().equals(coinId)) {
            return new AptosAddressGenerator();
        }
        if (Coins.SOL.coinId().equals(coinId)) {
            String code = Utilities.getCurrentSolAccount(getApplication());
            return new SolanaAddressGenerator(code);
        }
        if (Coins.isPolkadotFamily(Coins.coinCodeFromCoinId(coinId))) {
            if (Coins.DOT.coinId().equals(coinId)) {
                return new SubstrateAddressGenerator(Coins.DOT);
            }
            if (Coins.KSM.coinId().equals(coinId)) {
                return new SubstrateAddressGenerator(Coins.KSM);
            }
        }

        return null;
    }
}
