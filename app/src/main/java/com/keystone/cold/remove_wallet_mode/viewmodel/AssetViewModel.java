package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetsLiveData;

import java.util.List;

public class AssetViewModel extends AndroidViewModel {

    private final AssetsLiveData assetsLiveData;

    public AssetViewModel(@NonNull Application application) {
        super(application);
        assetsLiveData = new AssetsLiveData();
        assetsLiveData.setValue(null);
        assetsLiveData.addDataSource();
    }


    public LiveData<List<AssetItem>> loadAssets() {
        return assetsLiveData;
    }


    public void toggleAssetItem(AssetItem assetItem) {
        assetsLiveData.toggleAssetItem(assetItem);
    }

    public void toggleAssetItem(List<AssetItem> assetItems) {
        assetsLiveData.toggleAssetItem(assetItems);
    }
}
