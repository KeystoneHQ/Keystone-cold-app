package com.keystone.cold.remove_wallet_mode.helper.setup;

import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;

public abstract class ED25519CoinCreator extends BaseCreator {

    public ED25519CoinCreator(Coins.Coin coin) {
        super(coin);
    }

    @Override
    protected void update() {

    }

    @Override
    protected void addAccount(CoinEntity entity) {
        AccountEntity account = new AccountEntity();
        String defaultHdPath = CoinPath.M()
                .purpose(Coins.purposeNumber(entity.getCoinCode()))
                .coinType(entity.getIndex())
                .account(0)
                .toString();
        defaultHdPath += "/0'/0'";
        account.setHdPath(defaultHdPath);
        entity.addAccount(account);
    }
}
