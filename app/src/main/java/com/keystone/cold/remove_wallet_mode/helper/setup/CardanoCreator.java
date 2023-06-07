package com.keystone.cold.remove_wallet_mode.helper.setup;

import android.util.Log;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.viewmodel.ADASetupManager;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CardanoCreator extends BaseCreator {
    public CardanoCreator() {
        super(Coins.ADA);
    }

    // create cardano coin and accounts first.
    @Override
    public void setUp() {
        CoinEntity coinEntity = repository.loadCoinSync(Coins.ADA.coinId());
        if (coinEntity == null) {
            coinEntity = mapToCoinEntity();
            long id = repository.insertCoin(coinEntity);
            for (AccountEntity account : coinEntity.getAccounts()) {
                account.setCoinId(id);
            }
            repository.insertAccounts(coinEntity.getAccounts());
        } else {
            update();
        }
    }

    @Override
    protected void update() {
        CoinEntity coinEntity = repository.loadCoinSync(Coins.ADA.coinId());
        if (coinEntity != null) {
            List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
            boolean needsAddAccounts = accountEntities.size() < Coins.ADA.getAccounts().length;
            if (needsAddAccounts) {
                for (String account : Coins.ADA.getAccounts()) {
                    Optional<AccountEntity> optional = coinEntity.getAccounts().stream().filter(accountEntity -> accountEntity.getHdPath().equals(account)).findAny();
                    if (!optional.isPresent()) {
                        AccountEntity accountEntity = new AccountEntity();
                        accountEntity.setHdPath(account);
                        accountEntity.setCoinId(coinEntity.getId());
                        coinEntity.addAccount(accountEntity);
                        repository.insertAccount(accountEntity);
                    }
                }
                repository.updateCoin(coinEntity);
            }
            for (AccountEntity accountEntity : accountEntities) {
                String path = accountEntity.getHdPath();
                String xpub = ADASetupManager.getInstance().getXPub(path);
                if (xpub != null) {
                    accountEntity.setExPub(xpub);
                    repository.updateAccount(accountEntity);
                }
            }
        }
    }

    private CoinEntity mapToCoinEntity() {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(Coins.ADA.coinId());
        entity.setName(Coins.ADA.coinName());
        entity.setCoinCode(Coins.ADA.coinCode());
        entity.setIndex(Coins.ADA.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(MainApplication.getApplication()));
        entity.setAddressCount(0);
        entity.setShow(false);
        addAccount(entity);
        return entity;
    }

    @Override
    protected void generateDefaultAddress(CoinEntity coinEntity) {

    }

    @Override
    protected void addAccount(CoinEntity entity) {
        for (String account :
                Coins.ADA.getAccounts()) {
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setHdPath(account);
            String xpub = ADASetupManager.getInstance().getXPub(account);
            if (xpub != null) {
                accountEntity.setExPub(xpub);
            }
            entity.addAccount(accountEntity);
        }
    }
}
