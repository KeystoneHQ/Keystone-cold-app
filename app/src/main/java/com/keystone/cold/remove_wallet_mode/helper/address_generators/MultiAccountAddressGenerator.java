package com.keystone.cold.remove_wallet_mode.helper.address_generators;


import com.keystone.cold.db.entity.AccountEntity;

import java.util.List;

public abstract class MultiAccountAddressGenerator extends BaseAddressGenerator {

    protected String code;

    public MultiAccountAddressGenerator(String code) {
        this.code = code;
    }

    //Force subclasses to override the method
    @Override
    protected AccountEntity getAccountOfAddAddress(List<AccountEntity> accountEntities) {
        return getAccountByRule(accountEntities);
    }

    protected abstract AccountEntity getAccountByRule(List<AccountEntity> accountEntities);
}
