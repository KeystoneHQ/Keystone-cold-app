package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.keystone.cold.remove_wallet_mode.helper.WalletMapToCoinHelper;


public class SelectAddressViewModel extends AndroidViewModel {

    public SelectAddressViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getCoinId(String walletId) {
        MutableLiveData<String> observeCoinId = new MutableLiveData<>();
        observeCoinId.postValue(WalletMapToCoinHelper.mapToCoinId(walletId));
        return observeCoinId;
    }
}
