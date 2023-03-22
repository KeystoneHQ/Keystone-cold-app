package com.keystone.cold.remove_wallet_mode.helper.tx_loader;

import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.helper.PageStatusHelper;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EthTxLoader implements TxLoader {

    private final DataRepository repository = MainApplication.getApplication().getRepository();


    //预留 可以根据 accountCode 过滤地址
    private String accountCode;
    private String assetCoinId;

    public EthTxLoader(String code, String assetCoinId) {
        this.accountCode = code;
        this.assetCoinId = assetCoinId;
    }

    public EthTxLoader(String code) {
        this.accountCode = code;
        this.assetCoinId = Coins.ETH.coinId();
    }

    @Override
    public List<Tx> load() {
        return loadEthTxs()
                .stream()
                .filter(this::filterSomeTxs)
                .collect(Collectors.toList());
    }

    public void load(MutableLiveData<List<Tx>> txRecords) {
        List<Tx> txList = new ArrayList<>();
        web3TxEntityLoadCount = 0;
        txEntityLoadCount = 0;
        List<Web3TxEntity> web3TxEntities = getWeb3TxList();
        List<TxEntity> txEntities = getTxList();
        while ((!web3TxEntities.isEmpty() || !txEntities.isEmpty()) && PageStatusHelper.getInstance().getStatus()) {
            if (!web3TxEntities.isEmpty()) {
                txList.addAll(web3TxEntities.stream()
                        .map(this::mapToGenericETHTxEntity)
                        .filter((v) -> {
                            try {
                                JSONObject addition = new JSONObject(v.getAddition());
                                return filterTxsByETHAccount(addition);
                            } catch (JSONException e) {
                                return true;
                            }
                        })
                        .filter(this::filterSomeTxs)
                        .collect(Collectors.toList()));
                web3TxEntities = getWeb3TxList();
            }
            if (!txEntities.isEmpty()) {
                txList.addAll(txEntities.stream()
                        .map(this::mapToGenericETHTxEntity)
                        .filter((v) -> {
                            //Keystone Txs
                            return accountCode.equals(ETHAccount.BIP44_STANDARD.getCode());
                        })
                        .filter(this::filterSomeTxs)
                        .collect(Collectors.toList()));
                txEntities = getTxList();
            }
            txRecords.postValue(txList.stream()
                    .sorted((o1, o2) -> (int) (o2.getTimeStamp() - o1.getTimeStamp()))
                    .collect(Collectors.toList()));
        }
    }

    private int web3TxEntityLoadCount = 0;

    private List<Web3TxEntity> getWeb3TxList() {
        return repository.loadETHTxs(20, 20 * web3TxEntityLoadCount++);
    }

    private int txEntityLoadCount = 0;

    private List<TxEntity> getTxList() {
        return repository.loadTxs(Coins.ETH.coinId(), 20, 20 * txEntityLoadCount++);
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

    protected boolean filterTxsByETHAccount(JSONObject addition) {
        ETHAccount currentAccount = ETHAccount.ofCode(accountCode);
        ETHAccount targetAccount = ETHAccount.ofCode(addition.optString("signBy", ETHAccount.BIP44_STANDARD.getCode()));
        return currentAccount.equals(targetAccount);
    }

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
            AssetItem chain = transaction.getAssetItem();
            if (chain == null) {
                //this transaction belongs to ETH if we do not know this chain.
                return assetCoinId.equals(Coins.ETH.coinId());
            }
            //only show transaction belongs to this chain
            return assetCoinId.equals(chain.getCoinId());
        } else {
            //should not goes here;
            return false;
        }
    }


    private GenericETHTxEntity mapToGenericETHTxEntity(TxEntity txEntity) {
        GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
        genericETHTxEntity.setTxId(txEntity.getTxId());
        genericETHTxEntity.setSignedHex(txEntity.getSignedHex());
        genericETHTxEntity.setBelongTo(txEntity.getBelongTo());
        genericETHTxEntity.setTimeStamp(txEntity.getTimeStamp());
        genericETHTxEntity.setAddition(txEntity.getAddition());
        return genericETHTxEntity;
    }

    private GenericETHTxEntity mapToGenericETHTxEntity(Web3TxEntity web3TxEntity) {
        GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
        genericETHTxEntity.setTxId(web3TxEntity.getTxId());
        genericETHTxEntity.setTxType(web3TxEntity.getTxType());
        genericETHTxEntity.setSignedHex(web3TxEntity.getSignedHex());
        genericETHTxEntity.setAddition(web3TxEntity.getAddition());
        genericETHTxEntity.setBelongTo(web3TxEntity.getBelongTo());
        genericETHTxEntity.setTimeStamp(web3TxEntity.getTimeStamp());
        return genericETHTxEntity;
    }


}
