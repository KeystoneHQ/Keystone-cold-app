package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SubstrateAddressGenerator;

public class SubstrateCreator extends BaseCreator {

    public SubstrateCreator(Coins.Coin coin) {
        super(coin);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new SubstrateAddressGenerator(coin).generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setCoinId(entity.getId());
        accountEntity.setHdPath(coin.getAccounts()[0]);
        entity.addAccount(accountEntity);
    }
}
