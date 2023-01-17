package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.AptosAddressGenerator;

public class AptosCreator extends ED25519CoinCreator {

    public AptosCreator() {
        super(Coins.APTOS);
    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new AptosAddressGenerator().generateAddress(1);
    }

}
