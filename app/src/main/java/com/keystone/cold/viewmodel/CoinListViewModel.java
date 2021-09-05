/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.viewmodel;

import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.GenericETHTxEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.model.Coin;
import com.keystone.cold.model.Tx;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CoinListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;
    public static final Comparator<CoinEntity> coinEntityComparator = (o1, o2) -> {
        if (o1.getCoinCode().equals(Coins.BTC.coinCode())) {
            return -1;
        } else if (o2.getCoinCode().equals(Coins.BTC.coinCode())) {
            return 1;
        } else if (o1.getCoinCode().equals(Coins.ETH.coinCode())) {
            return -1;
        } else if (o2.getCoinCode().equals(Coins.ETH.coinCode())) {
            return 1;
        } else {
            return o1.getCoinCode().compareTo(o2.getCoinCode());
        }
    };

    public CoinListViewModel(@NonNull Application application) {
        super(application);

        mObservableCoins = new MediatorLiveData<>();
        mObservableCoins.setValue(null);

        mRepository = ((MainApplication) application).getRepository();

        mObservableCoins.addSource(mRepository.loadCoins(), mObservableCoins::setValue);
    }

    public LiveData<List<CoinEntity>> getCoins() {
        return mObservableCoins;
    }

    public void toggleCoin(Coin coin) {
        CoinEntity entity = new CoinEntity(coin);
        entity.setShow(!coin.isShow());
        mRepository.updateCoin(entity);
    }

    public LiveData<CoinEntity> loadCoin(int id) {
        return mRepository.loadCoin(id);
    }

    public LiveData<TxEntity> loadTx(String txId) {
        return mRepository.loadTx(txId);
    }

    public LiveData<GenericETHTxEntity> loadETHTx(String txId) {
        MutableLiveData<GenericETHTxEntity> genericETHTxEntityLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                GenericETHTxEntity genericETHTxEntity = mRepository.loadETHTxSync(txId);
                JSONObject ethTx = EthImpl.decodeEIP1559Transaction(genericETHTxEntity.getSignedHex(), null);
                if (ethTx == null) {
                    genericETHTxEntityLiveData.postValue(null);
                    return;
                }
                genericETHTxEntity.setChainId(ethTx.getInt("chainId"));
                genericETHTxEntity.setSignature(EthImpl.getSignature(genericETHTxEntity.getSignedHex()));
                genericETHTxEntity.setMemo(ethTx.getString("data"));
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                genericETHTxEntity.setTo(ethTx.getString("to"));
                BigDecimal amount = new BigDecimal(ethTx.getString("value"));
                double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
                genericETHTxEntity.setAmount(nf.format(value) + " ETH");
                calculateDisplayEIP1559Fee(ethTx, genericETHTxEntity);
                genericETHTxEntity.setMemo(ethTx.getString("data"));
                genericETHTxEntity.setBelongTo(mRepository.getBelongTo());
                genericETHTxEntityLiveData.postValue(genericETHTxEntity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return genericETHTxEntityLiveData;
    }

    public LiveData<GenericETHTxEntity> loadEIP1559ETHTx(String txId) {
        MutableLiveData<GenericETHTxEntity> genericETHTxEntityLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                GenericETHTxEntity genericETHTxEntity = mRepository.loadETHTxSync(txId);
                JSONObject ethTx = EthImpl.decodeEIP1559Transaction(genericETHTxEntity.getSignedHex(), null);
                if (ethTx == null) {
                    genericETHTxEntityLiveData.postValue(null);
                    return;
                }
                genericETHTxEntity.setChainId(ethTx.getInt("chainId"));
                genericETHTxEntity.setSignature(EthImpl.getEIP1559Signature(genericETHTxEntity.getSignedHex()));
                genericETHTxEntity.setMemo(ethTx.getString("data"));
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                genericETHTxEntity.setTo(ethTx.getString("to"));
                BigDecimal amount = new BigDecimal(ethTx.getString("value"));
                double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
                genericETHTxEntity.setAmount(nf.format(value) + " ETH");
                calculateDisplayEIP1559Fee(ethTx, genericETHTxEntity);
                genericETHTxEntity.setMemo(ethTx.getString("data"));
                genericETHTxEntity.setBelongTo(mRepository.getBelongTo());
                genericETHTxEntityLiveData.postValue(genericETHTxEntity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return genericETHTxEntityLiveData;
    }


    private void calculateDisplayEIP1559Fee(JSONObject ethTx, GenericETHTxEntity tx) throws JSONException {
        NumberFormat nf = NumberFormat.getInstance();
        BigDecimal gasPriorityPrice = new BigDecimal(ethTx.getString("maxPriorityFeePerGas"));
        BigDecimal gasLimitPrice = new BigDecimal(ethTx.getString("maxFeePerGas"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        BigDecimal estimatedFee = BigDecimal.valueOf(gasLimitPrice.multiply(gasLimit).doubleValue() - gasPriorityPrice.doubleValue())
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal maxFee = BigDecimal.valueOf(gasLimitPrice.multiply(gasLimit).doubleValue() - gasLimitPrice.doubleValue())
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP);
        tx.setMaxPriorityFeePerGas(nf.format(gasPriorityPrice) + " GWEI");
        tx.setMaxFeePerGas(nf.format(gasLimitPrice) + " GWEI");
        tx.setGasLimit(nf.format(gasLimit));
        tx.setEstimatedFee(nf.format(estimatedFee) + " ETH");
        tx.setMaxFee(nf.format(maxFee) + " ETH");
    }


    public LiveData<List<GenericETHTxEntity>> loadEthTxs() {
        final MutableLiveData<List<GenericETHTxEntity>>[] listMutableLiveData = new MutableLiveData[]{new MutableLiveData<>()};
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GenericETHTxEntity> ethTxEntityList = insertTxEntitytoETHTable();
            List<GenericETHTxEntity> genericETHTxEntities = mRepository.loadETHTxsSync();
            if (genericETHTxEntities != null) {
                genericETHTxEntities.addAll(ethTxEntityList);
            } else {
                genericETHTxEntities = ethTxEntityList;
            }
            listMutableLiveData[0].postValue(genericETHTxEntities);
        });
        return listMutableLiveData[0];
    }

    private List<GenericETHTxEntity> insertTxEntitytoETHTable() {
        List<TxEntity> txEntities = mRepository.loadAllTxSync(Coins.ETH.coinId());
        if (txEntities == null) return null;
        List<GenericETHTxEntity> ethTxEntityList = new ArrayList<>();
        txEntities = txEntities.stream()
                .filter(this::shouldShow)
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
            ethTxEntityList.add(genericETHTxEntity);
        }
        return ethTxEntityList;
    }

    private boolean shouldShow(Tx tx) {
        WatchWallet watchWallet = WatchWallet.getWatchWallet(getApplication());
        boolean shouldShow = tx.getSignId().equals(watchWallet.getSignId());
        return shouldShow && Utilities.getCurrentBelongTo(getApplication()).equals(tx.getBelongTo());
    }

    public List<AccountEntity> loadAccountForCoin(CoinEntity coin) {
        return mRepository.loadAccountsForCoin(coin);
    }

}
