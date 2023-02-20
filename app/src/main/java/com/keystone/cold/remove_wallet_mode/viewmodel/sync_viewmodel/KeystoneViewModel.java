package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.protocol.EncodeConfig;
import com.keystone.cold.protocol.builder.SyncBuilder;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.List;

public class KeystoneViewModel extends AndroidViewModel {
    private final DataRepository mRepository;

    public KeystoneViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    private boolean isSupported(CoinEntity coinEntity) {
        for (Coins.Coin supportedCoin : WatchWallet.KEYSTONE.getSupportedCoins()) {
            if (supportedCoin.coinCode().equals(coinEntity.getCoinCode())) {
                return true;
            }
        }
        return false;
    }

    public LiveData<String> generateSyncKeystone() {
        MutableLiveData<String> sync = new MutableLiveData<>();
        sync.setValue("");
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CoinEntity> coinEntities = mRepository.loadCoinsSync();
            SyncBuilder syncBuilder = new SyncBuilder(EncodeConfig.DEFAULT);
            for (CoinEntity entity : coinEntities) {
                if (!isSupported(entity)) continue;
                SyncBuilder.Coin coin = new SyncBuilder.Coin();
                coin.setActive(entity.isShow());
                coin.setCoinCode(entity.getCoinCode());
                List<AccountEntity> accounts = mRepository.loadAccountsForCoin(entity);
                for (AccountEntity accountEntity : accounts) {
                    SyncBuilder.Account account = new SyncBuilder.Account();
                    account.addressLength = accountEntity.getAddressLength();
                    account.hdPath = accountEntity.getHdPath();
                    account.xPub = accountEntity.getExPub();
                    if (TextUtils.isEmpty(account.xPub)) {
                        continue;
                    }
                    account.isMultiSign = false;
                    coin.addAccount(account);
                    if (coin.coinCode.equals(Coins.ETH.coinCode())) {
                        break;
                    }
                }
                if (coin.accounts.size() > 0) {
                    syncBuilder.addCoin(coin);
                }
            }
            if (syncBuilder.getCoinsCount() == 0) {
                sync.postValue("");
            } else {
                sync.postValue(syncBuilder.build());
            }

        });
        return sync;
    }
}
