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

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CoinViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private static LiveData<CoinEntity> mObservableCoin;
    private final LiveData<List<AddressEntity>> mObservableAddress;
    public final ObservableField<CoinEntity> coin = new ObservableField<>();

    private CoinViewModel(@NonNull Application application, DataRepository repository,
                          final String coinId) {
        super(application);
        mRepository = repository;
        mObservableCoin = repository.loadCoin(coinId);
        mObservableAddress = repository.loadAddress(coinId);

    }

    public LiveData<CoinEntity> getObservableCoin() {
        return mObservableCoin;
    }

    public LiveData<List<AddressEntity>> getAddress() {
        return mObservableAddress;
    }

    public LiveData<List<AddressEntity>> getNewAddressLiveData(String coinId) {
        return mRepository.loadAddress(coinId);
    }

    public void setCoin(CoinEntity coin) {
        this.coin.set(coin);
    }

    public void updateAddress(AddressEntity addr) {
        AppExecutors.getInstance().diskIO().execute(() -> mRepository.updateAddress(addr));
    }

    public void preGenerateSolDerivationAddress() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String masterFingerPrint = new GetMasterFingerprintCallable().call();
            String preDerivationPaths = Utilities.getSolDerivationPaths(getApplication());
            if (!TextUtils.isEmpty(preDerivationPaths)) {
                try {
                    JSONObject jsonObject = new JSONObject(preDerivationPaths);
                    String preMasterFingerprint = jsonObject.optString("master_fingerprint");
                    JSONObject accountPaths = jsonObject.optJSONObject("sol_derivation_paths");
                    if (masterFingerPrint.equalsIgnoreCase(preMasterFingerprint)
                            && accountPaths != null
                            && !TextUtils.isEmpty(accountPaths.toString())
                            && !accountPaths.toString().equals(new JSONObject().toString())) {
                        return;
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
            try {
                JSONObject derivationPaths = new JSONObject();
                JSONObject accountPaths = new JSONObject();
                SOLAccount[] solAccounts = new SOLAccount[]{SOLAccount.SOLFLARE_BIP44_ROOT, SOLAccount.SOLFLARE_BIP44, SOLAccount.SOLFLARE_BIP44_CHANGE};
                for (int i = 0; i < solAccounts.length; i++) {
                    JSONArray addresses = new JSONArray();
                    AccountEntity accountEntity = mRepository.loadTargetSOLAccount(solAccounts[i]);
                    if (accountEntity == null) {
                        continue;
                    }
                    int addressLength = 3;
                    if (solAccounts[i] == SOLAccount.SOLFLARE_BIP44_ROOT) {
                        addressLength = 1;
                    }
                    for (int index = 0; index < addressLength; index++) {
                        String address = AddAddressViewModel.deriveSolAddress(accountEntity, index, null);
                        addresses.put(address);
                    }
                    accountPaths.put(solAccounts[i].getCode(), addresses);
                }
                derivationPaths.put("sol_derivation_paths", accountPaths);
                derivationPaths.put("version", 1);
                derivationPaths.put("master_fingerprint", masterFingerPrint);
                Utilities.setSolDerivationPaths(getApplication(), derivationPaths.toString());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        });

    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;
        private final String mCoinId;
        private final DataRepository mRepository;

        public Factory(@NonNull Application application, String coinId) {
            mApplication = application;
            mCoinId = coinId;
            mRepository = ((MainApplication) application).getRepository();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new CoinViewModel(mApplication, mRepository, mCoinId);
        }
    }
}
