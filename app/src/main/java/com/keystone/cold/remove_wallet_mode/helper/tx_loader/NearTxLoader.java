package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.TxEntity;

public class NearTxLoader extends GeneralTxLoader {

    private final String accountCode;

    public NearTxLoader(String code) {
        super(Coins.NEAR.coinId());
        this.accountCode = code;
    }

    @Override
    protected boolean filterSomeTxs(TxEntity txEntity) {
        NEARAccount nearAccount = NEARAccount.ofCode(accountCode);
        return nearAccount.isBelongCurrentAccount(txEntity.getAddition());
    }
}
