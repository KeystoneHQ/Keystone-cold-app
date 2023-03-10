package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.DashAddressGenerator;

public class DashCreator extends BaseCreator {
    public DashCreator() {
        super(Coins.DASH);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new DashAddressGenerator().generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity accountEntity = new AccountEntity();
        String xpub = new GetExtendedPublicKeyCallable(DashAddressGenerator.PATH).call();
        accountEntity.setHdPath(DashAddressGenerator.PATH);
        accountEntity.setExPub(xpub);
        entity.addAccount(accountEntity);
    }
}
