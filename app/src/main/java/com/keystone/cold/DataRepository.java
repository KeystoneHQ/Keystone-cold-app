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

package com.keystone.cold;

import static com.keystone.coinlib.utils.Coins.DOT;
import static com.keystone.coinlib.utils.Coins.KSM;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.db.AppDatabase;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.db.entity.Web3TxEntity;
import com.keystone.cold.db.entity.WhiteListEntity;
import com.keystone.cold.model.Coin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataRepository {
    private static DataRepository sInstance;

    private final AppDatabase mDb;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;
    private final Context context;

    private DataRepository(Context context, final AppDatabase database) {
        mDb = database;
        this.context = context;
        mObservableCoins = new MediatorLiveData<>();
        mObservableCoins.addSource(mDb.coinDao().loadAllCoins(), coins -> {
            if (mDb.getDatabaseCreated().getValue() != null) {
                mObservableCoins.postValue(filterByBelongTo(coins));
            }
        });
    }

    public String getBelongTo() {
        return Utilities.getCurrentBelongTo(context);
    }

    public static DataRepository getInstance(Context context, final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(context, database);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<CoinEntity>> loadCoins() {
        return mObservableCoins;
    }

    public LiveData<List<CoinEntity>> reloadCoins() {
        MediatorLiveData<List<CoinEntity>> coins = new MediatorLiveData<>();
        coins.addSource(mDb.coinDao().loadAllCoins(), coinEntities -> {
            if (mDb.getDatabaseCreated().getValue() != null) {
                coins.postValue(filterByBelongTo(coinEntities));
            }
        });
        return coins;
    }

    public List<CoinEntity> loadCoinsSync() {
        return filterByBelongTo(mDb.coinDao().loadAllCoinsSync());
    }

    public void updateCoin(Coin coin) {
        mDb.coinDao().update(new CoinEntity(coin));
    }

    public void insertAddress(List<AddressEntity> addrs) {
        mDb.addressDao().insertAll(addrs);
    }

    public void insertAddress(AddressEntity addrs) {
        mDb.addressDao().insertAddress(addrs);
    }

    public LiveData<CoinEntity> loadCoin(final long id) {
        return mDb.coinDao().loadCoin(id);
    }

    public CoinEntity loadCoinSync(final String coinId) {
        return mDb.coinDao().loadCoinSync(coinId, getBelongTo());
    }

    public LiveData<CoinEntity> loadCoin(final String coinId) {
        return mDb.coinDao().loadCoin(coinId, getBelongTo());
    }

    public LiveData<List<AddressEntity>> loadAddress(String coinId) {
        return mDb.addressDao().loadAddressForCoin(coinId, getBelongTo());
    }

    public List<AddressEntity> loadAddressSync(String coinId) {
        return mDb.addressDao().loadAddressSync(coinId, getBelongTo());
    }

    public AddressEntity loadAddressBypath(String path) {
        if (path.equals(DOT.getAccounts()[0]) || path.equals(KSM.getAccounts()[0])) {
            return mDb.addressDao().loadAddress(path, getBelongTo());
        }
        return mDb.addressDao().loadAddress(path.toUpperCase(), getBelongTo());
    }

    public void updateAddress(AddressEntity addressEntity) {
        mDb.addressDao().update(addressEntity);
    }

    public void deleteAddressByCoin(CoinEntity coin) {
        mDb.addressDao().deleteAddressByCoin(coin.getCoinId(), getBelongTo());
    }

    public LiveData<List<TxEntity>> loadTxs(String coinId) {
        return mDb.txDao().loadTxs(coinId);
    }

    public LiveData<List<TxEntity>> loadAllTxs() {
        return mDb.txDao().loadAllTxs();
    }

    public List<TxEntity> loadElectrumTxsSync(String coinId) {
        return mDb.txDao().loadElectrumTxsSync(coinId);
    }

    public List<TxEntity> loadAllTxSync(String coinId) {
        return mDb.txDao().loadTxsSync(coinId);
    }

    public LiveData<TxEntity> loadTx(String txId) {
        return mDb.txDao().load(txId);
    }

    public TxEntity loadTxSync(String txId) {
        return mDb.txDao().loadSync(txId);
    }

    public void insertTx(TxEntity tx) {
        mDb.txDao().insert(tx);
    }

    public void insertCoins(List<CoinEntity> coins) {
        mDb.runInTransaction(() -> mDb.coinDao().insertAll(coins));
    }

    public long insertCoin(CoinEntity coin) {
        return mDb.coinDao().insert(coin);
    }

    public void deleteCoin(CoinEntity coin) {
        mDb.coinDao().deleteCoin(coin.getCoinId(), getBelongTo());
    }

    public void clearDb() {
        mDb.clearAllTables();
    }

    private List<CoinEntity> filterByBelongTo(List<CoinEntity> coins) {
        String belongTo = Utilities.getCurrentBelongTo(context);
        return coins.isEmpty() ? Collections.emptyList()
                :
                coins.stream()
                        .filter(coin -> belongTo.equals(coin.getBelongTo()))
                        .collect(Collectors.toList());
    }


    public LiveData<List<WhiteListEntity>> loadWhiteList() {
        return mDb.whiteListDao().load();
    }

    public void insertWhiteList(WhiteListEntity entity) {
        mDb.whiteListDao().insert(entity);
    }

    public void deleteWhiteList(WhiteListEntity entity) {
        mDb.whiteListDao().delete(entity);
    }

    public WhiteListEntity queryWhiteList(String address) {
        return mDb.whiteListDao().queryAddress(address, Utilities.getCurrentBelongTo(context));
    }

    public long insertAccount(AccountEntity account) {
        return mDb.accountDao().add(account);
    }

    public void updateAccount(AccountEntity account) {
        mDb.accountDao().update(account);
    }

    public List<AccountEntity> loadAccountsForCoin(CoinEntity coin) {
        return mDb.accountDao().loadForCoin(coin.getId());
    }

    public void deleteAccountsByCoin(CoinEntity coin){
        mDb.accountDao().deleteByCoin(coin.getId());
    }

    public AccountEntity loadTargetETHAccount(ETHAccount account){
        try {
            CoinEntity coinEntity = this.loadCoinSync(Coins.ETH.coinId());
            List<AccountEntity> accountEntityList = this.loadAccountsForCoin(coinEntity);
            for (int i = 0; i < accountEntityList.size(); i++) {
                JSONObject jsonObject = new JSONObject(accountEntityList.get(i).getAddition());
                if (jsonObject.get("eth_account").equals(account.getCode())) {
                    return accountEntityList.get(i);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public AccountEntity loadTargetSOLAccount(SOLAccount account){
        try {
            CoinEntity coinEntity = this.loadCoinSync(Coins.SOL.coinId());
            if (coinEntity == null) {
                return null;
            }
            List<AccountEntity> accountEntityList = this.loadAccountsForCoin(coinEntity);
            for (int i = 0; i < accountEntityList.size(); i++) {
                String addition = accountEntityList.get(i).getAddition();
                if (TextUtils.isEmpty(addition)) {
                    continue;
                }
                JSONObject jsonObject = new JSONObject(addition);
                if (jsonObject.get("sol_account").equals(account.getCode())) {
                    return accountEntityList.get(i);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public CoinEntity loadCoinEntityByCoinCode(String coinCode) {
        String coinId = Coins.coinIdFromCoinCode(coinCode);
        return loadCoinSync(coinId);
    }

    public void deleteHiddenVaultData() {
        mDb.coinDao().deleteHidden();
        mDb.txDao().deleteHidden();
        mDb.addressDao().deleteHidden();
        mDb.whiteListDao().deleteHidden();
        mDb.ethTxDao().deleteHidden();
    }

    public void insertETHTx(Web3TxEntity tx) {
        mDb.ethTxDao().insert(tx);
    }

    public Web3TxEntity loadETHTxSync(String txId) {
        return mDb.ethTxDao().loadSync(txId);
    }

    public List<Web3TxEntity> loadETHTxsSync() {
        return mDb.ethTxDao().loadETHTxsSync();
    }
}
