package com.keystone.cold.remove_wallet_mode.helper.setup;

import static com.keystone.coinlib.utils.Coins.isDefaultOpen;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;

public class ArweaveCreator extends Creator {
    protected final DataRepository repository = MainApplication.getApplication().getRepository();

    //for arweave require at least 40s to generate public key, we only create coin and account for arweave now;
    //we generate address on the ARAuthFragment;
    @Override
    public void setUp() {
        CoinEntity coinEntity = repository.loadCoinSync(Coins.AR.coinId());
        if (coinEntity == null) {
            coinEntity = mapToCoinEntity();
            long id = repository.insertCoin(coinEntity);
            AccountEntity accountEntity = coinEntity.getAccounts().get(0);
            accountEntity.setCoinId(id);
            repository.insertAccount(accountEntity);
        }
    }

    private CoinEntity mapToCoinEntity() {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(Coins.AR.coinId());
        entity.setName(Coins.AR.coinName());
        entity.setCoinCode(Coins.AR.coinCode());
        entity.setIndex(Coins.AR.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(MainApplication.getApplication()));
        entity.setAddressCount(0);
        entity.setShow(isDefaultOpen(Coins.AR.coinCode()));

        AccountEntity accountEntity = new AccountEntity();
        // AR public key is stored in AccountEntity.addition.public_key
        accountEntity.setHdPath(Coins.AR.getAccounts()[0]);
        entity.addAccount(accountEntity);
        return entity;
    }
}
