package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import java.util.List;
import java.util.stream.Collectors;

public class EvmosTxLoader extends EthTxLoader {

    public EvmosTxLoader() {
        super(ETHAccount.BIP44_STANDARD.getCode());
    }

    @Override
    public List<Tx> load() {
        List<Tx> originTxList = new GeneralTxLoader(Coins.EVMOS.coinId()).load();
        originTxList.addAll(super.load());
        return originTxList.stream()
                .sorted((o1, o2) -> (int) (o2.getTimeStamp() - o1.getTimeStamp()))
                .collect(Collectors.toList());
    }


    @Override
    protected boolean filterSomeTxs(GenericETHTxEntity ethTxEntity) {
        String signData = ethTxEntity.getSignedHex();
        EthereumTransaction transaction = null;
        switch (ethTxEntity.getTxType()) {
            case 0x00: {
                transaction = EthereumTransaction.generateLegacyTransaction(signData, null, true);
                break;
            }
            case 0x02: {
                transaction = EthereumTransaction.generateFeeMarketTransaction(signData, null);
                break;
            }
            default:
                break;
        }
        if (transaction != null) {
            return transaction.getChainId() == 9000 || transaction.getChainId() == 9001;
        }
        return false;
    }
}
