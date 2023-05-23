package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.keystone.coinlib.utils.Coins;

public class FewchaWalletViewModel extends BaseCryptoMultiAccountsSyncViewModel {

    public FewchaWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }
}
