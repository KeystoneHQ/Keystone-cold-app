package com.keystone.cold.util;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.main.SyncInfo;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.List;
import java.util.stream.Collectors;

public class SyncAddressUtil {

    public interface Callback {
        void oneAddress(SyncInfo syncInfoList);

        void moreThanOneAddress();

        void noAddress();
    }

    public static void getSyncAddressInfo(String coinId, String code, WatchWallet watchWallet, Callback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DataRepository repository = MainApplication.getApplication().getRepository();
            List<AddressEntity> addressEntities = repository.loadAddressSync(coinId);
            addressEntities = addressEntities.stream()
                    .filter(entity -> isCurrentAccount(code, entity, watchWallet))
                    .collect(Collectors.toList());

            int size = addressEntities.size();
            if (size == 0) {
                AppExecutors.getInstance().mainThread().execute(callback::noAddress);
            } else if (size == 1) {
                AddressEntity addressEntity = addressEntities.get(0);
                SyncInfo syncInfo = new SyncInfo();
                syncInfo.setCoinId(addressEntity.getCoinId());
                syncInfo.setAddress(addressEntity.getAddressString());
                syncInfo.setPath(addressEntity.getPath());
                syncInfo.setName(addressEntity.getName());
                syncInfo.setAddition(addressEntity.getAddition());
                AppExecutors.getInstance().mainThread().execute(() -> callback.oneAddress(syncInfo));
            } else {
                AppExecutors.getInstance().mainThread().execute(callback::moreThanOneAddress);
            }
        });

    }

    private static boolean isCurrentAccount(String code, AddressEntity entity, WatchWallet watchWallet) {
        switch (watchWallet) {
            case SOLANA: {
                SOLAccount account = SOLAccount.ofCode(code);
                return account.isChildrenPath(entity.getPath());
            }
            case NEAR: {
                NEARAccount account = NEARAccount.ofCode(code);
                return account.isChildrenPath(entity.getPath());
            }
            default:
                return true;
        }
    }
}
