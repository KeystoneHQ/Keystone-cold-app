package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

public class BitcoinCashAddressGenerator extends BaseAddressGenerator {
    public static final String PATH = "M/44'/145'/0'";

    public BitcoinCashAddressGenerator() {
        coinId = Coins.BCH.coinId();
    }

    @Override
    protected String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver) {
        String path = PATH + "/0/" + index;
        String xPub = ExtendedPublicKeyCacheHelper.getInstance().getExtendedPublicKey(path);
        String address = deriver.derive(xPub);
        addressEntity.setPath(path);
        return address;
    }
}
