package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.TxEntity;

public class SolTxLoader extends GeneralTxLoader {

    private final String accountCode;

    public SolTxLoader(String code) {
        super(Coins.SOL.coinId());
        this.accountCode = code;
    }


    @Override
    protected boolean filterSomeTxs(TxEntity txEntity) {
        SOLAccount solAccount = SOLAccount.ofCode(accountCode);
        return solAccount.isBelongCurrentAccount(txEntity.getAddition());
    }
}
