package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;

public class SubstrateWalletViewModel extends AndroidViewModel {
    private Long addressId;
    private String coinId;

    public SubstrateWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public MutableLiveData<String> generateSyncData() {
        MutableLiveData<String> result = new MutableLiveData<>(null);
        if (coinId == null || addressId == null) return result;
        AppExecutors.getInstance().diskIO().execute(() -> {
            AddressEntity addressEntity = MainApplication.getApplication().getRepository().loadAddressById(addressId);
            String prefix = "substrate";
            String address = addressEntity.getAddressString();
            String genesisHash = getGenesisHash(Coins.coinCodeFromCoinId(coinId));
            String name = addressEntity.getName();
            result.postValue(prefix + ":" + address + ":" + genesisHash + ":" + name);
        });
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
