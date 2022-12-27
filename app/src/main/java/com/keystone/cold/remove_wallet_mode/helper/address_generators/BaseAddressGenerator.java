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

public abstract class BaseAddressGenerator implements AddressGenerator {

    protected String coinId;


    @Override
    public void generateAddress(int count, StatusCallBack statusCallBack) {
        addAddress(coinId, count, statusCallBack);
    }

    private void addAddress(final String coinId, int count, StatusCallBack statusCallBack) {
        DataRepository repository = MainApplication.getApplication().getRepository();
        CoinEntity coinEntity = repository.loadCoinSync(coinId);
        AccountEntity accountEntity = repository.loadAccountsForCoin(coinEntity).get(0);
        int addressLength = accountEntity.getAddressLength();
        int targetAddressCount = addressLength + count;
        List<AddressEntity> entities = generateAddress(accountEntity, coinEntity, count);
        if (entities == null || entities.isEmpty()) {
            statusCallBack.onFail();
            return;
        }
        coinEntity.setAddressCount(targetAddressCount);
        accountEntity.setAddressLength(targetAddressCount);
        repository.updateAccount(accountEntity);
        repository.updateCoin(coinEntity);
        repository.insertAddress(entities);
        statusCallBack.onSuccess();
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
                if (existAddress(coinEntity.getCoinId(), addressEntity.getPath())) {
                    continue;
                }
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
        return repository.loadAddressByPathAndCoinId(coinId, path) != null;
    }

    protected abstract String deriveAddress(int index, AddressEntity addressEntity, AbsDeriver deriver);
}
