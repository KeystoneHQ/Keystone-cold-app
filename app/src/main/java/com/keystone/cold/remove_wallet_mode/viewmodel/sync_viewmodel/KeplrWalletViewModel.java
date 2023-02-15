package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.main.SyncInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeplrWalletViewModel extends BaseCryptoMultiAccountsSyncViewModel {

    public KeplrWalletViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected List<SyncInfo> getSyncInfos() {
        Set<Integer> coinIndexSet = new HashSet<>();
        return Arrays.stream(Wallet.KEPLR.getSupportedCoins())
                .filter(coin -> filterRepeatedCoinIndex(coinIndexSet, coin))
                .map(this::getCoinAddress)
                .map(this::addressEntityToSyncInfo)
                .collect(Collectors.toList());
    }


    private boolean filterRepeatedCoinIndex(Set<Integer> coinIndexSet, Coins.Coin coin) {
        if (!coinIndexSet.contains(coin.coinIndex())) {
            coinIndexSet.add(coin.coinIndex());
            return true;
        }
        return false;
    }

    private AddressEntity getCoinAddress(Coins.Coin coin) {
        List<AddressEntity> addressEntities = MainApplication.getApplication().getRepository().loadAddressSync(coin.coinId());
        return addressEntities.get(0);
    }
}
