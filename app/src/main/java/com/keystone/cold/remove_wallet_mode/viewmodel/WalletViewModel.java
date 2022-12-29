package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.SyncPageJumpHelper;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;
import com.keystone.cold.remove_wallet_mode.ui.status.AddressDetectStatus;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

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

    public LiveData<AddressDetectStatus> detectWalletItem(WalletItem walletItem) {
        MutableLiveData<AddressDetectStatus> stepMode = new MutableLiveData<>();
        if (Wallet.isSingleChainWallet(walletItem.getWalletId())) {  // Enter the processing flow of the single-chain wallet
            SyncPageJumpHelper.detect(walletItem.getWalletId(), stepMode);
        } else { //Enter the processing flow of the multi-chain wallet, compared with the single-chain wallet, there is usually one more page for selecting the coin
            stepMode.postValue(AddressDetectStatus.MULTI_CHAINS);
        }
        return stepMode;
    }
}
