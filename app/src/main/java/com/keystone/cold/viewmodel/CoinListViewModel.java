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

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.db.viewmodel.CoinModel;
import com.keystone.cold.model.Tx;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CoinListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;
    private boolean isFromTFCard;
    public static final Comparator<CoinEntity> coinEntityComparator = (o1, o2) -> {
        if (o1.getCoinCode().equals(Coins.BTC.coinCode())) {
            return -1;
        } else if (o2.getCoinCode().equals(Coins.BTC.coinCode())) {
            return 1;
        } else if (o1.getCoinCode().equals(Coins.BTC_LEGACY.coinCode())) {
            return -1;
        } else if (o2.getCoinCode().equals(Coins.BTC_LEGACY.coinCode())) {
            return 1;
        } else if (o1.getCoinCode().equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) {
            return -1;
        } else if (o2.getCoinCode().equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) {
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

    public void toggleCoin(CoinModel coin) {
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

    public LiveData<List<TxEntity>> loadTxs(String coinId) {
        return mRepository.loadTxs(coinId);
    }

    public LiveData<List<TxEntity>> loadAllTxs() {
        return mRepository.loadAllTxs();
    }

    public LiveData<GenericETHTxEntity> loadETHTx(String txId) {
        MutableLiveData<GenericETHTxEntity> genericETHTxEntityLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            GenericETHTxEntity genericETHTxEntity = GenericETHTxEntity.transformDbEntity(mRepository.loadETHTxSync(txId));
            genericETHTxEntityLiveData.postValue(genericETHTxEntity);
        });
        return genericETHTxEntityLiveData;
    }

    public LiveData<List<GenericETHTxEntity>> loadEthTxs() {
        final MutableLiveData<List<GenericETHTxEntity>>[] listMutableLiveData = new MutableLiveData[]{new MutableLiveData<>()};
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<GenericETHTxEntity> genericETHTxEntities = getGenericETHTxsFromETHTXDBEntities();
            List<GenericETHTxEntity> ethTxEntityList = getGenericETHTxsFromTxEntities();
            if (genericETHTxEntities != null) {
                if (ethTxEntityList != null) {
                    genericETHTxEntities.addAll(ethTxEntityList);
                }
            } else {
                genericETHTxEntities = ethTxEntityList;
            }
            listMutableLiveData[0].postValue(genericETHTxEntities);
        });
        return listMutableLiveData[0];
    }

    private List<GenericETHTxEntity> getGenericETHTxsFromETHTXDBEntities() {
        List<Web3TxEntity> ethTxDBEntities = mRepository.loadETHTxsSync();
        if (ethTxDBEntities == null) return null;
        List<GenericETHTxEntity> ethTxEntityList = new ArrayList<>();
        for (Web3TxEntity web3TxEntity : ethTxDBEntities) {
            GenericETHTxEntity genericETHTxEntity = new GenericETHTxEntity();
            genericETHTxEntity.setTxId(web3TxEntity.getTxId());
            genericETHTxEntity.setTxType(web3TxEntity.getTxType());
            genericETHTxEntity.setSignedHex(web3TxEntity.getSignedHex());
            genericETHTxEntity.setAddition(web3TxEntity.getAddition());
            genericETHTxEntity.setBelongTo(web3TxEntity.getBelongTo());
            ethTxEntityList.add(genericETHTxEntity);
        }
        return ethTxEntityList;
    }

    private List<GenericETHTxEntity> getGenericETHTxsFromTxEntities() {
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
            genericETHTxEntity.setBelongTo(txEntity.getBelongTo());
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

    public boolean isFromTFCard() {
        return isFromTFCard;
    }
}
