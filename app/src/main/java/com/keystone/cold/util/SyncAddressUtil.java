package com.keystone.cold.util;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class SyncAddressUtil {

    public interface Callback {
        void onGetAddressInfo(String addressInfo);

        void onError();
    }

    public static void getSyncAddressInfo(String coinId, String code, WatchWallet watchWallet, Callback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DataRepository repository = MainApplication.getApplication().getRepository();
            List<AddressEntity> addressEntities = repository.loadAddressSync(coinId);
            addressEntities = addressEntities.stream()
                    .filter(entity -> isCurrentAccount(code, entity, watchWallet))
                    .collect(Collectors.toList());
            if (addressEntities.size() == 1) {
                AddressEntity addressEntity = addressEntities.get(0);
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("path", addressEntity.getPath());
                    jsonObject.put("address", addressEntity.getAddressString());
                    jsonObject.put("name", addressEntity.getName());
                    jsonArray.put(jsonObject);
                    String info = jsonArray.toString();
                    AppExecutors.getInstance().mainThread().execute(() -> callback.onGetAddressInfo(info));
                    return;
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
            AppExecutors.getInstance().mainThread().execute(() -> callback.onError());
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
                return false;
        }
    }
}
