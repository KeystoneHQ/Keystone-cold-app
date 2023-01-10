package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.model.Tx;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EthTxLoader implements TxLoader {

    private final DataRepository repository = MainApplication.getApplication().getRepository();


    //预留 可以根据 accountCode 过滤地址
    private String accountCode;


    public EthTxLoader(String code) {
        this.accountCode = code;
    }

    @Override
    public List<Tx> load() {
        return loadEthTxs()
                .stream()
                .filter(this::filterSomeTxs)
                .collect(Collectors.toList());
    }


    public List<GenericETHTxEntity> loadEthTxs() {

        List<GenericETHTxEntity> genericETHTxEntities = getGenericETHTxsFromETHTXDBEntities();
        List<GenericETHTxEntity> ethTxEntityList = getGenericETHTxsFromTxEntities();
        if (genericETHTxEntities != null) {
            if (ethTxEntityList != null) {
                genericETHTxEntities.addAll(ethTxEntityList);
            }
        } else {
            genericETHTxEntities = ethTxEntityList;
        }
        return genericETHTxEntities;
    }

    private List<GenericETHTxEntity> getGenericETHTxsFromETHTXDBEntities() {
        List<Web3TxEntity> ethTxDBEntities = repository.loadETHTxsSync();
        if (ethTxDBEntities == null) return null;
        List<GenericETHTxEntity> ethTxEntityList = new ArrayList<>();
        for (Web3TxEntity web3TxEntity : ethTxDBEntities) {
            GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
            genericETHTxEntity.setTxId(web3TxEntity.getTxId());
            genericETHTxEntity.setTxType(web3TxEntity.getTxType());
            genericETHTxEntity.setSignedHex(web3TxEntity.getSignedHex());
            genericETHTxEntity.setAddition(web3TxEntity.getAddition());
            genericETHTxEntity.setBelongTo(web3TxEntity.getBelongTo());
            genericETHTxEntity.setTimeStamp(web3TxEntity.getTimeStamp());
            ethTxEntityList.add(genericETHTxEntity);
        }
        return ethTxEntityList;
    }

    private List<GenericETHTxEntity> getGenericETHTxsFromTxEntities() {
        List<TxEntity> txEntities = repository.loadAllTxSync(Coins.ETH.coinId());
        if (txEntities == null) return null;
        List<GenericETHTxEntity> ethTxEntityList = new ArrayList<>();
        txEntities = txEntities.stream()
                .sorted((Comparator<Tx>) (o1, o2) -> {
                    if (o1.getSignId().equals(o2.getSignId())) {
                        return (int) (o2.getTimeStamp() - o1.getTimeStamp());
                    } else if (ELECTRUM_SIGN_ID.equals(o1.getSignId())) {
                        return -1;
                    } else {
                        return 1;
                    }
                })
                .collect(Collectors.toList());
        for (TxEntity txEntity : txEntities) {
            GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
            genericETHTxEntity.setTxId(txEntity.getTxId());
            genericETHTxEntity.setSignedHex(txEntity.getSignedHex());
            genericETHTxEntity.setBelongTo(txEntity.getBelongTo());
            genericETHTxEntity.setTimeStamp(txEntity.getTimeStamp());
            ethTxEntityList.add(genericETHTxEntity);
        }
        return ethTxEntityList;
    }

    protected boolean filterSomeTxs(GenericETHTxEntity ethTxEntity) {
        return true;
    }

}
