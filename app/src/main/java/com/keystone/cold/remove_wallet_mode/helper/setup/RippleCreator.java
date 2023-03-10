package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.RippleAddressGenerator;

public class RippleCreator extends BaseCreator {
    public RippleCreator() {
        super(Coins.XRP);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {
        new RippleAddressGenerator().generateAddress(1);
    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity accountEntity = new AccountEntity();
        String xpub = new GetExtendedPublicKeyCallable(RippleAddressGenerator.PATH).call();
        accountEntity.setHdPath(RippleAddressGenerator.PATH);
        accountEntity.setExPub(xpub);
        entity.addAccount(accountEntity);
    }
}
