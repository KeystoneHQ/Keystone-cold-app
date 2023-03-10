package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.LitecoinAddressGenerator;

public class LitecoinCreator extends BaseCreator{
    public LitecoinCreator() {
        super(Coins.LTC);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new LitecoinAddressGenerator().generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity accountEntity = new AccountEntity();
        String xpub = new GetExtendedPublicKeyCallable(LitecoinAddressGenerator.PATH).call();
        accountEntity.setHdPath(LitecoinAddressGenerator.PATH);
        accountEntity.setExPub(xpub);
        entity.addAccount(accountEntity);
    }
}
