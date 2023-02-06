package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.AddressFilter;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.EthereumAddressFilter;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.GeneralAddressFilter;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.NearAddressFilter;
import com.keystone.cold.remove_wallet_mode.helper.address_filter.SolanaAddressFilter;

public class AddressFilterManager {

    public static boolean filterAddress(AddressEntity entity) {
        String coinId = entity.getCoinId();
        AddressFilter addressFilter = getAddressFilter(coinId);
        return addressFilter.filter(entity);
    }

    private static AddressFilter getAddressFilter(String coinId) {
        if (Coins.ETH.coinId().equals(coinId)) {
            return new EthereumAddressFilter();
        } else if (Coins.SOL.coinId().equals(coinId)) {
            return new SolanaAddressFilter();
        } else if (Coins.NEAR.coinId().equals(coinId)) {
            return new NearAddressFilter();
        }else {
            return new GeneralAddressFilter();
        }
    }
}
