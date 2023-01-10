package com.keystone.cold.remove_wallet_mode.helper.tx_loader;


import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.model.Tx;

import java.util.List;
import java.util.stream.Collectors;

public class GeneralTxLoader implements TxLoader {

    private final DataRepository repository = MainApplication.getApplication().getRepository();

    private final String coinId;

    public GeneralTxLoader(String coinId) {
        this.coinId = coinId;
    }

    @Override
    public List<Tx> load() {
        return repository.loadAllTxSync(coinId).stream()
                .filter(this::filterSomeTxs).collect(Collectors.toList());
    }



    protected boolean filterSomeTxs(TxEntity txEntity) {
        return true;
    }

}
