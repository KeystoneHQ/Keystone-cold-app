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
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddAddressViewModel extends AndroidViewModel {

    private final DataRepository mRepo;
    public CoinEntity coin;
    private final ObservableField<Boolean> loading = new ObservableField<>();
    private final MutableLiveData<Boolean> addComplete = new MutableLiveData<>();

    public AddAddressViewModel(@NonNull Application application) {
        super(application);
        mRepo = ((MainApplication)application).getRepository();
    }

    public ObservableField<Boolean> getLoading() {
        return loading;
    }

    public void addAddress(List<String> addrs) {
        loading.set(true);
        new AddAddressTask(coin, mRepo, () -> {
            loading.set(false);
            addComplete.setValue(Boolean.TRUE);
        }).execute(addrs.toArray(new String[0]));
    }

    public void addAddress(CoinEntity coinEntity, DataRepository repo, String addrName) {
        new AddAddressTask(coinEntity, repo, null).execute(addrName);
    }

    public CoinEntity getCoin(String coinId) {
        coin = mRepo.loadCoinSync(coinId);
        return coin;
    }

    public LiveData<Boolean> getObservableAddState() {
        return addComplete;
    }

    public void addEthAccountAddress(int number, CoinEntity coinEntity, Runnable onComplete){
        String code = Utilities.getCurrentEthAccount(getApplication());
        ETHAccount account = ETHAccount.ofCode(code);
        AccountEntity accountEntity = mRepo.loadTargetETHAccount(account);
        if(accountEntity != null) {
            addEthAccountAddress(accountEntity, mRepo, number, coinEntity, onComplete);
        }
        AppExecutors.getInstance().mainThread().execute(onComplete);
    }

    public static void addEthAccountAddress(AccountEntity accountEntity, DataRepository repository, int number, CoinEntity coinEntity, Runnable onComplete) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AbsDeriver deriver = AbsDeriver.newInstance("ETH");
            if (deriver == null) {
                Log.e("addEthAccountAddress", "deriver is null");
            } else {
                int addressLength = accountEntity.getAddressLength();
                List<AddressEntity> addressEntities = new ArrayList<>();
                int targetAddressCount = addressLength + number;
                for (int index = addressLength; index < targetAddressCount; index++) {
                    AddressEntity addressEntity = new AddressEntity();
                    String addr = deriveETHAddress(accountEntity, index, addressEntity);
                    addressEntity.setAddressString(addr);
                    addressEntity.setCoinId(Coins.ETH.coinId());
                    addressEntity.setIndex(index);
                    addressEntity.setName("ETH-" + index);
                    addressEntity.setBelongTo(coinEntity.getBelongTo());
                    if (repository.loadAddressBypath(addressEntity.getPath()) != null) {
                        continue;
                    }
                    addressEntities.add(addressEntity);
                    if(ETHAccount.isStandardChildren(addressEntity.getPath())) {
                        coinEntity.setAddressCount(coinEntity.getAddressCount() + 1);
                    }
                }
                accountEntity.setAddressLength(targetAddressCount);
                repository.updateAccount(accountEntity);
                repository.insertAddress(addressEntities);
                repository.updateCoin(coinEntity);
            }
            AppExecutors.getInstance().mainThread().execute(onComplete);
        });
    }

    public static String deriveETHAddress(AccountEntity accountEntity, int index, AddressEntity addressEntity) {
        ETHAccount ethAccount = ETHAccount.ofCode(accountEntity.getETHAccountCode());
        boolean isSetPath = addressEntity != null;
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
                if (isSetPath) addressEntity.setPath(xPubPath + "/0/0");
                break;
            case LEDGER_LEGACY:
                xPubPath = ETHAccount.LEDGER_LEGACY.getPath();
                xPub = accountEntity.getExPub();
                address = deriver.derive(xPub, index);
                if (isSetPath) addressEntity.setPath(xPubPath + "/" + index);
                break;
            case BIP44_STANDARD:
                xPubPath = ETHAccount.BIP44_STANDARD.getPath();
                xPub = accountEntity.getExPub();
                address = deriver.derive(xPub, 0, index);
                if (isSetPath) addressEntity.setPath(xPubPath + "/0/" + index);
                break;
            default:
                break;
        }
        return address;
    }


    public void addSolAccountAddress(int number, CoinEntity coinEntity, Runnable onComplete) {
        String code = Utilities.getCurrentSolAccount(getApplication());
        SOLAccount account = SOLAccount.ofCode(code);
        AccountEntity accountEntity = mRepo.loadTargetSOLAccount(account);
        if (accountEntity != null) {
            addSolAccountAddress(accountEntity, mRepo, number, coinEntity, onComplete);
        } else {
            AppExecutors.getInstance().mainThread().execute(onComplete);
        }
    }

    public static void addSolAccountAddress(AccountEntity accountEntity, DataRepository repository, int number, CoinEntity coinEntity, Runnable onComplete) {

        AppExecutors.getInstance().diskIO().execute(() -> {

            AbsDeriver deriver = AbsDeriver.newInstance("SOL");
            if (deriver == null) {
                Log.e("addSolAccountAddress", "deriver is null");
            } else {
                int addressLength = accountEntity.getAddressLength();
                int targetAddressCount = addressLength + number;
                List<AddressEntity> entities = new ArrayList<>();
                for (int index = addressLength; index < targetAddressCount; index++) {
                    AddressEntity addressEntity = new AddressEntity();
                    String addr = deriveSolAddress(accountEntity, index, addressEntity);
                    if (repository.loadAddressBypath(addressEntity.getPath()) != null) {
                        continue;
                    }
                    addressEntity.setAddressString(addr);
                    addressEntity.setCoinId(coinEntity.getCoinId());
                    addressEntity.setIndex(index);
                    addressEntity.setName("SOL-" + index);
                    addressEntity.setBelongTo(coinEntity.getBelongTo());
                    entities.add(addressEntity);
                }
                coinEntity.setAddressCount(targetAddressCount);
                accountEntity.setAddressLength(targetAddressCount);
                repository.updateAccount(accountEntity);
                repository.updateCoin(coinEntity);
                repository.insertAddress(entities);
            }
            AppExecutors.getInstance().mainThread().execute(onComplete);
        });
    }

    public static String deriveSolAddress(AccountEntity accountEntity, int index, AddressEntity addressEntity) {
        String address = "";
        SOLAccount solAccount = SOLAccount.ofAddition(accountEntity.getAddition());
        AbsDeriver deriver = AbsDeriver.newInstance("SOL");
        if (deriver == null) {
            return address;
        }
        boolean isSetPath = addressEntity != null;

        String xPub = null;
        String xPubPath = null;
        switch (solAccount) {
            case SOLFLARE_BIP44_ROOT:
                xPubPath = "M/44'/501'";
                break;
            case SOLFLARE_BIP44:
                xPubPath = "M/44'/501'" + "/" + index + "'";
                break;
            case SOLFLARE_BIP44_CHANGE:
                xPubPath = "M/44'/501'" + "/" + index + "'/0'";
                break;
        }
        xPub = new GetExtendedPublicKeyCallable(xPubPath).call();
        address = deriver.derive(xPub);

        if (isSetPath){
            addressEntity.setPath(xPubPath);

            try {
                JSONObject innerJson = new JSONObject();
                innerJson.put("derivation_path", solAccount.getCode());
                innerJson.put("index", index);
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(innerJson);
                JSONObject addition = new JSONObject();
                addition.put("addition", jsonArray);

                //json定义详见 com.keystone.cold.db.entity.AddressEntity addition字段
                addressEntity.setAddition(addition.toString());
            } catch (JSONException exception){
                exception.printStackTrace();
            }
        }

        return address;
    }

    public static class AddAddressTask extends AsyncTask<String, Void, Void> {
        private final CoinEntity coinEntity;
        private final DataRepository repo;
        private final Runnable onComplete;

        public AddAddressTask(CoinEntity coinEntity, DataRepository repo, Runnable onComplete) {
            this.coinEntity = coinEntity;
            this.repo = repo;
            this.onComplete = onComplete;
        }

        @Override
        protected Void doInBackground(String... strings) {

            AccountEntity defaultAccount = repo.loadAccountsForCoin(coinEntity).get(0);
            String path = defaultAccount.getHdPath();
            int addressCount = coinEntity.getAddressCount();

            String exPub = defaultAccount.getExPub();
            if (TextUtils.isEmpty(exPub)) {
                exPub = new GetExtendedPublicKeyCallable(path).call();
                defaultAccount.setExPub(exPub);
            }

            List<AddressEntity> entities = new ArrayList<>();
            AbsDeriver deriver = AbsDeriver.newInstance(coinEntity.getCoinCode());
            for (int i = 0; i < strings.length; i++) {
                AddressEntity addressEntity = new AddressEntity();
                int change = 0;
                int index = i + addressCount;
                if (Coins.isPolkadotFamily(coinEntity.getCoinCode())) {
                    addressEntity.setPath(defaultAccount.getHdPath());
                } else {
                    addressEntity.setPath(String.format(path + "/%s/%s", change, index));
                }

                if (deriver != null) {
                    String addr = deriver.derive(exPub, change, index);
                    addressEntity.setAddressString(addr);
                    addressEntity.setCoinId(coinEntity.getCoinId());
                    addressEntity.setIndex(i + addressCount);
                    addressEntity.setName(strings[i]);
                    addressEntity.setBelongTo(coinEntity.getBelongTo());
                    entities.add(addressEntity);
                }
            }

            coinEntity.setAddressCount(coinEntity.getAddressCount() + strings.length);
            defaultAccount.setAddressLength(addressCount + strings.length);
            repo.updateAccount(defaultAccount);
            repo.updateCoin(coinEntity);
            repo.insertAddress(entities);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (onComplete != null) {
                onComplete.run();
            }
        }

    }
}
