package com.keystone.cold.remove_wallet_mode.helper.setup;

import static com.keystone.cold.remove_wallet_mode.helper.address_generators.SubstrateAddressGenerator.CHAIN_DOT;
import static com.keystone.cold.remove_wallet_mode.helper.address_generators.SubstrateAddressGenerator.CHAIN_KSM;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SubstrateAddressGenerator;

public class SubstrateCreator extends BaseCreator {

    public SubstrateCreator(Coins.Coin coin) {
        super(coin);;
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        if (coin.equals(Coins.DOT)) {
            new SubstrateAddressGenerator(CHAIN_DOT).generateAddress(1);
        } else {
            new SubstrateAddressGenerator(CHAIN_KSM).generateAddress(1);
        }
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setCoinId(entity.getId());
        accountEntity.setHdPath(coin.getAccounts()[0]);
        entity.addAccount(accountEntity);
    }
}
