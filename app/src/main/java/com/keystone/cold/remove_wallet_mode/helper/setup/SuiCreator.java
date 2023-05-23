package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SuiAddressGenerator;

public class SuiCreator extends ED25519CoinCreator {

    public SuiCreator() {
        super(Coins.SUI);
    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new SuiAddressGenerator().generateAddress(1);
    }
}
