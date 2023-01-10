package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.entity.TxEntity;

public class SolTxLoader extends GeneralTxLoader {

    private String accountCode;

    public SolTxLoader(String code) {
        super(Coins.SOL.coinId());
        this.accountCode = code;
    }


    //todo  filter by code
    @Override
    protected boolean filterSomeTxs(TxEntity txEntity) {
        return super.filterSomeTxs(txEntity);
    }
}
