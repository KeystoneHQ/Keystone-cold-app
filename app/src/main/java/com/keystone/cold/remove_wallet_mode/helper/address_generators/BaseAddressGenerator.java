package com.keystone.cold.remove_wallet_mode.helper.address_generators;

import android.util.Log;

import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseAddressGenerator implements AddressGenerator {

    protected String coinId;


    @Override
    public void generateAddress(int count, StatusCallBack statusCallBack) {
        addAddress(coinId, count, statusCallBack);
    }


    public void generateAddress(int count) {
        addAddress(coinId, count, null);
    }

    private void addAddress(final String coinId, int count, StatusCallBack statusCallBack) {
        DataRepository repository = MainApplication.getApplication().getRepository();
        CoinEntity coinEntity = repository.loadCoinSync(coinId);
        AccountEntity accountEntity = getAccountOfAddAddress(repository.loadAccountsForCoin(coinEntity));
        if (accountEntity == null) {
            if (statusCallBack != null) statusCallBack.onFail();
            return;
        }
        int addressLength = accountEntity.getAddressLength();
        int targetAddressCount = addressLength + count;
        List<AddressEntity> entities = generateAddress(accountEntity, coinEntity, count);
        if (entities == null || entities.isEmpty()) {
            if (statusCallBack != null) statusCallBack.onFail();
            return;
        }
        coinEntity.setAddressCount(targetAddressCount);
        accountEntity.setAddressLength(targetAddressCount);
        repository.updateAccount(accountEntity);
        repository.updateCoin(coinEntity);
        entities = entities.stream().filter(entity -> !existAddress(coinId, entity.getPath())).collect(Collectors.toList());
        repository.insertAddress(entities);
        if (statusCallBack != null) statusCallBack.onSuccess();
    }

    //The coins of single account take the first account, and the coin of multiple accounts needs to rewrite this function
    protected AccountEntity getAccountOfAddAddress(List<AccountEntity> accountEntities) {
        return accountEntities.get(0);
    }

    private List<AddressEntity> generateAddress(AccountEntity accountEntity, CoinEntity coinEntity, int count) {
        AbsDeriver deriver = AbsDeriver.newInstance(coinEntity.getCoinCode());
        if (deriver == null) {
            Log.e("GenerateAddressError", coinEntity.getCoinCode() + "deriver is null");
            return null;
        } else {
            int addressLength = accountEntity.getAddressLength();
            int targetAddressCount = addressLength + count;
            List<AddressEntity> entities = new ArrayList<>();
            for (int index = addressLength; index < targetAddressCount; index++) {
                AddressEntity addressEntity = generateAddressEntity(coinEntity, index, deriver);
                entities.add(addressEntity);
            }
            return entities;
        }
    }


    private AddressEntity generateAddressEntity(CoinEntity coinEntity, int index, AbsDeriver deriver) {
        AddressEntity addressEntity = new AddressEntity();
        String address = deriveAddress(index, addressEntity, deriver);
        addressEntity.setAddressString(address);
        addressEntity.setCoinId(coinEntity.getCoinId());
        addressEntity.setIndex(index);
        addressEntity.setName(coinEntity.getCoinCode() + "-" + index);
        addressEntity.setBelongTo(coinEntity.getBelongTo());
        return addressEntity;
    }

    private boolean existAddress(String coinId, String path) {
        DataRepository repository = MainApplication.getApplication().getRepository();
        return repository.loadAddressByPathAndCoinId(path, coinId) != null;
    }

    protected abstract String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver);
}
