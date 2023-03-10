package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.SyncMode;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;

import java.util.ArrayList;
import java.util.List;

public class WalletViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WalletItem>> observableWallets;

    public WalletViewModel(@NonNull Application application) {
        super(application);
        observableWallets = new MutableLiveData<>();
    }

    public LiveData<List<WalletItem>> getObservableWallets() {
        return observableWallets;
    }

    public void setWalletInfos(String[] walletNames, String[] walletValues, String[] summaries) {
        if (walletNames == null || walletValues == null || summaries == null) {
            observableWallets.postValue(null);
            return;
        }
        if (walletNames.length != walletValues.length || walletNames.length != summaries.length) {
            observableWallets.postValue(null);
            return;
        }
        List<WalletItem> walletItems = new ArrayList<>();
        for (int i = 0; i < walletNames.length; i++) {
            walletItems.add(new WalletItem(walletValues[i], walletNames[i], summaries[i]));
        }
        observableWallets.postValue(walletItems);
    }

    public LiveData<SyncMode> determineSyncMode(String  walletId) {
        MutableLiveData<SyncMode> stepMode = new MutableLiveData<>();
        SyncMode.detect(walletId, stepMode);
        return stepMode;
    }
}
