package com.keystone.cold.integration.cosmoswallet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.main.SyncInfo;
import com.keystone.cold.util.URRegistryHelper;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoMultiAccounts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeplrWalletViewModel extends AndroidViewModel {
    public KeplrWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UR> generateSyncData() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur = generateCryptoMultiAccounts().toUR();
            data.postValue(ur);
        });
        return data;
    }

    private CryptoMultiAccounts generateCryptoMultiAccounts() {
        Set<Integer> coinIndexSet = new HashSet<>();
        List<SyncInfo> syncInfoList =
                Arrays.stream(WatchWallet.KEPLR_WALLET.getSupportedCoins())
                        .filter(coin -> filterRepeatedCoinIndex(coinIndexSet, coin))
                        .map(this::getSyncInfo)
                        .collect(Collectors.toList());
        return URRegistryHelper.generateCryptoMultiAccounts(syncInfoList);
    }

    private boolean filterRepeatedCoinIndex(Set<Integer> coinIndexSet, Coins.Coin coin) {
        if (!coinIndexSet.contains(coin.coinIndex())) {
            coinIndexSet.add(coin.coinIndex());
            return true;
        }
        return false;
    }

    private SyncInfo getSyncInfo(Coins.Coin coin) {
        List<AddressEntity> addressEntities = MainApplication.getApplication().getRepository().loadAddressSync(coin.coinId());
        AddressEntity addressEntity = addressEntities.get(0);
        SyncInfo syncInfo = new SyncInfo();
        syncInfo.setCoinId(addressEntity.getCoinId());
        syncInfo.setAddress(addressEntity.getAddressString());
        syncInfo.setPath(addressEntity.getPath());
        syncInfo.setName(addressEntity.getName());
        syncInfo.setAddition(addressEntity.getAddition());
        return syncInfo;
    }
}
