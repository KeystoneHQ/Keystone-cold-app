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
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.protocol.EncodeConfig;
import com.keystone.cold.protocol.builder.SyncBuilder;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.UR;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SyncViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final MutableLiveData<ETHAccount> chainsMutableLiveData;
    private final MutableLiveData<SOLAccount> solAccountMutableLiveData;


    public SyncViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
        chainsMutableLiveData = new MutableLiveData<>();
        chainsMutableLiveData.postValue(ETHAccount.ofCode(Utilities.getCurrentEthAccount(application)));

        solAccountMutableLiveData = new MutableLiveData<>();
        solAccountMutableLiveData.postValue(SOLAccount.ofCode(Utilities.getCurrentSolAccount(application)));
    }

    public MutableLiveData<ETHAccount> getChainsMutableLiveData() {
        return chainsMutableLiveData;
    }

    public MutableLiveData<SOLAccount> getSolAccountMutableLiveData() {
        return solAccountMutableLiveData;
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
                    if(coin.coinCode.equals(Coins.ETH.coinCode())) {
                        break;
                    }
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

    public MutableLiveData<UR> generateSyncMetamaskUR(ETHAccount ethAccount) {
        chainsMutableLiveData.postValue(ethAccount);
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur;
            switch (ethAccount){
                case LEDGER_LIVE: {
                    ur = URRegistryHelper.generateCryptoAccountForLedgerLive(0, 10).toUR();
                    break;
                }
                case LEDGER_LEGACY: {
                    ur = URRegistryHelper.generateCryptoHDKeyForLedgerLegacy().toUR();
                    break;
                }
                default: {
                    ur = URRegistryHelper.generateCryptoHDKeyForETHStandard().toUR();
                }
            }
            data.postValue(ur);
        });
        return data;
    }

    public MutableLiveData<UR> generateSyncSolanaUR(List<Pair<String, String>> syncInfo) {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur;
            ur = URRegistryHelper.generateCryptoMultiAccountsForSolByAddress(syncInfo).toUR();
            data.postValue(ur);
        });
        return data;
    }

    public LiveData<String> generateSyncMetamask(ETHAccount ethAccount) {
        chainsMutableLiveData.postValue(ethAccount);
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            String ur;
            switch (ethAccount){
                case LEDGER_LIVE: {
                    ur = URRegistryHelper.generateCryptoAccountForLedgerLive(0, 10).toUR().toString();
                    break;
                }
                case LEDGER_LEGACY: {
                    ur = URRegistryHelper.generateCryptoHDKeyForLedgerLegacy().toUR().toString();
                    break;
                }
                default: {
                    ur = URRegistryHelper.generateCryptoHDKeyForETHStandard().toUR().toString();
                }
            }
            result.postValue(ur);
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
        AccountEntity accountEntity = mRepository.loadTargetETHAccount(ethAccount);
        for (int i = 0; i < 3; i++) {
            result.add(i, Pair.create("" + i, AddAddressViewModel.deriveETHAddress(accountEntity, i, null)));
        }
        return result;
    }

    public MutableLiveData<List<Pair<String, String>>> getSolAccounts(SOLAccount solAccount) {
        MutableLiveData<List<Pair<String, String>>> mutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Pair<String, String>> result = new ArrayList<>();
            String solDerivationPath = Utilities.getSolDerivationPaths(getApplication());
            if (!TextUtils.isEmpty(solDerivationPath)) {
                String masterFingerPrint = new GetMasterFingerprintCallable().call();
                try {
                    JSONObject derivationPaths = new JSONObject(solDerivationPath);
                    String preMasterFingerPrint = derivationPaths.optString("master_fingerprint");
                    if (masterFingerPrint.equalsIgnoreCase(preMasterFingerPrint)) {
                        JSONObject accountPaths = (JSONObject) derivationPaths.get("sol_derivation_paths");
                        JSONArray jsonArray = (JSONArray) accountPaths.get(solAccount.getCode());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            result.add(Pair.create("" + i, (String) jsonArray.get(i)));
                        }
                    } else {
                        result.addAll(getPairs(solAccount));
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            } else {
                result.addAll(getPairs(solAccount));
            }
            mutableLiveData.postValue(result);
        });
        return mutableLiveData;
    }


    @NonNull
    private List<Pair<String, String>> getPairs(SOLAccount solAccount) {
        List<Pair<String, String>> result = new ArrayList<>();
        AccountEntity accountEntity = mRepository.loadTargetSOLAccount(solAccount);
        if (accountEntity != null) {
            for (int i = 0; i < 3; i++) {
                result.add(i, Pair.create("" + i, AddAddressViewModel.deriveSolAddress(accountEntity, i, null)));
            }
        }
        return result;
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
