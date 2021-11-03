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

import android.app.Application;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.protocol.EncodeConfig;
import com.keystone.cold.protocol.builder.SyncBuilder;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;

import java.util.ArrayList;
import java.util.List;

public class SyncViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final MutableLiveData<ETHAccount> chainsMutableLiveData;

    public SyncViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
        chainsMutableLiveData = new MutableLiveData<>();
        chainsMutableLiveData.postValue(ETHAccount.LEDGER_LEGACY);
    }

    public MutableLiveData<ETHAccount> getChainsMutableLiveData() {
        return chainsMutableLiveData;
    }

    public List<AccountEntity> loadAccountForCoin(CoinEntity coin) {
        return mRepository.loadAccountsForCoin(coin);
    }

    private boolean isSupported(CoinEntity coinEntity) {
        for (Coins.Coin supportedCoin : WatchWallet.KEYSTONE.getSupportedCoins()) {
            if (supportedCoin.coinCode().equals(coinEntity.getCoinCode())) {
                return true;
            }
        }
        return false;
    }

    public LiveData<String> generateSyncKeystone() {
        MutableLiveData<String> sync = new MutableLiveData<>();
        sync.setValue("");
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CoinEntity> coinEntities = mRepository.loadCoinsSync();
            SyncBuilder syncBuilder = new SyncBuilder(EncodeConfig.DEFAULT);
            for (CoinEntity entity : coinEntities) {
                if (!isSupported(entity)) continue;
                SyncBuilder.Coin coin = new SyncBuilder.Coin();
                coin.setActive(entity.isShow());
                coin.setCoinCode(entity.getCoinCode());
                List<AccountEntity> accounts = loadAccountForCoin(entity);
                for (AccountEntity accountEntity : accounts) {
                    SyncBuilder.Account account = new SyncBuilder.Account();
                    account.addressLength = accountEntity.getAddressLength();
                    account.hdPath = accountEntity.getHdPath();
                    account.xPub = accountEntity.getExPub();
                    if (TextUtils.isEmpty(account.xPub)) {
                        continue;
                    }
                    account.isMultiSign = false;
                    coin.addAccount(account);
                }
                if (coin.accounts.size() > 0) {
                    syncBuilder.addCoin(coin);
                }
            }
            if (syncBuilder.getCoinsCount() == 0) {
                sync.postValue("");
            } else {
                sync.postValue(syncBuilder.build());
            }

        });
        return sync;
    }

    public LiveData<XrpSyncData> generateSyncXumm(final int index) {
        MutableLiveData<XrpSyncData> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity xrp = mRepository.loadCoinSync(Coins.XRP.coinId());
            String pubkey = Util.getPublicKeyHex(xrp.getExPub(), 0, index);
            for (AddressEntity addressEntity : mRepository.loadAddressSync(Coins.XRP.coinId())) {
                if (addressEntity.getIndex() == index) {
                    result.postValue(new XrpSyncData(addressEntity, pubkey));
                }
            }
        });
        return result;
    }

    public static class XrpSyncData {
        public AddressEntity addressEntity;
        public String pubkey;

        public XrpSyncData(AddressEntity addressEntity, String pubkey) {
            this.addressEntity = addressEntity;
            this.pubkey = pubkey;
        }
    }

    public LiveData<String> generateSyncPolkadotjs(String coinCode) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            AddressEntity addressEntity = mRepository.loadAddressSync(Coins.coinIdFromCoinCode(coinCode)).get(0);
            String prefix = "substrate";
            String address = addressEntity.getAddressString();
            String genesisHash = getGenesisHash(coinCode);
            String name = "keystone-" + Coins.coinNameFromCoinCode(coinCode);
            result.postValue(prefix + ":" + address + ":" + genesisHash + ":" + name);
        });
        return result;
    }

    public LiveData<String> generateSyncMetamask(ETHAccount ethAccount) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CryptoHDKey cryptoHDKey = URRegistryHelper.generateCryptoHDKey(ethAccount.getPath(), ethAccount.getType());
            result.postValue(cryptoHDKey.toUR().toString());
        });
        return result;
    }

    public MutableLiveData<List<Pair<String, String>>> getAccounts(ETHAccount ethAccount) {
        MutableLiveData<List<Pair<String, String>>> mutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Pair<String, String>> result = getPairs(ethAccount);
            mutableLiveData.postValue(result);
        });
        return mutableLiveData;
    }

    @NonNull
    private List<Pair<String, String>> getPairs(ETHAccount ethAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            result.add(i, Pair.create("Account " + i, getAddress(ethAccount, i)));
        }
        return result;
    }

    public String getAddress(ETHAccount ethAccount, int index) {
        String address = "";
        AbsDeriver deriver = AbsDeriver.newInstance("ETH");
        if (deriver == null) return address;
        String xPub;
        String xPubPath;
        switch (ethAccount) {
            case LEDGER_LIVE:
                xPubPath = ETHAccount.LEDGER_LIVE.getPath() + "/" + index + "'";
                xPub = new GetExtendedPublicKeyCallable(xPubPath).call();
                address = deriver.derive(xPub, 0 , 0);
                break;
            case LEDGER_LEGACY:
                xPubPath = ETHAccount.LEDGER_LEGACY.getPath() + "/" + index;
                xPub = new GetExtendedPublicKeyCallable(xPubPath).call();
                address = deriver.derive(xPub);
                break;
            case BIP44_STANDARD:
                xPubPath = ETHAccount.BIP44_STANDARD.getPath().substring(0, ETHAccount.BIP44_STANDARD.getPath().length() - 2);
                xPub = new GetExtendedPublicKeyCallable(xPubPath).call();
                address = deriver.derive(xPub, 0, index);
                break;
            default:
                break;
        }
        return address;
    }

    private String getGenesisHash(String coinCode) {
        switch (coinCode) {
            case "DOT":
                return "0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3";
            case "KSM":
                return "0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe";
        }
        return "";
    }
}
