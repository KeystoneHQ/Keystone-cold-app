package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.main.SyncInfo;
import com.keystone.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.UR;

import java.util.List;
import java.util.stream.Collectors;

public class BaseCryptoMultiAccountsSyncViewModel extends AndroidViewModel {

    protected String coinId;
    protected List<Long> addressIds;


    public BaseCryptoMultiAccountsSyncViewModel(@NonNull Application application) {
        super(application);
    }

    public void setAddressIds(List<Long> addressIds) {
        this.addressIds = addressIds;
    }

    public MutableLiveData<UR> generateSyncUR() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur = URRegistryHelper.generateCryptoMultiAccounts(getSyncInfos()).toUR();
            data.postValue(ur);
        });
        return data;
    }

    protected List<SyncInfo> getSyncInfos() {
        List<AddressEntity> addressEntities = MainApplication.getApplication().getRepository().loadAddressSync(coinId);
        return addressEntities.stream()
                .filter(this::filterSomeAddress)
                .map(this::addressEntityToSyncInfo)
                .collect(Collectors.toList());
    }

    protected boolean filterSomeAddress(AddressEntity addressEntity) {
        if (addressIds == null || addressIds.isEmpty()) {
            return true;
        }
        return addressIds.contains(addressEntity.getId());
    }

    protected SyncInfo addressEntityToSyncInfo(AddressEntity addressEntity) {
        SyncInfo syncInfo = new SyncInfo();
        syncInfo.setCoinId(addressEntity.getCoinId());
        syncInfo.setAddress(addressEntity.getAddressString());
        syncInfo.setPath(addressEntity.getPath());
        syncInfo.setName(addressEntity.getName());
        syncInfo.setAddition(addressEntity.getAddition());
        return syncInfo;
    }
}
