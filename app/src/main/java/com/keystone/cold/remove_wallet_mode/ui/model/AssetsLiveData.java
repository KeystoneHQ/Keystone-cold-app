package com.keystone.cold.remove_wallet_mode.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.CoinConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssetsLiveData extends MediatorLiveData<List<AssetItem>> {

    private final List<AssetItem> fromDBItems = new ArrayList<>();
    private final List<AssetItem> fromConfigItems = new ArrayList<>();

    private MutableLiveData<List<AssetItem>> extraAssetItem;


    public void addDataSource() {
        addConfigDataSource(getAssetListFromConfig());
        addDBDataSource(getAssetListFromDB());
    }

    public void addDBDataSource(LiveData<List<AssetItem>> source) {
        addSource(source, assetItems -> {
            fromDBItems.clear();
            fromDBItems.addAll(assetItems);
            postValue(getAllData());
        });
    }

    public void addConfigDataSource(LiveData<List<AssetItem>> source) {
        addSource(source, assetItems -> {
            fromConfigItems.clear();
            fromConfigItems.addAll(assetItems);
            postValue(getAllData());
        });
    }

    private List<AssetItem> getAllData() {
        List<AssetItem> allItems = new ArrayList<>();
        allItems.addAll(fromConfigItems);
        allItems.addAll(fromDBItems);
        CoinConfigHelper.sortCoinList(allItems);
        return allItems;
    }

    public void toggleAssetItem(List<AssetItem> assetItems) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            for (AssetItem assetItem : assetItems) {
                toggleAssetItemInCallThread(assetItem);
            }
        });
    }

    public void toggleAssetItem(AssetItem assetItem) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            toggleAssetItemInCallThread(assetItem);
        });
    }

    private void toggleAssetItemInCallThread(AssetItem assetItem) {
        if (CoinConfigHelper.coinInLocalConfig(assetItem.getCoinId())) {
            CoinConfigHelper.toggleLocalCoin(assetItem.getCoinId());
            getAssetListFromConfig().postValue(CoinConfigHelper.getExtraCoins());
        } else {
            DataRepository repository = MainApplication.getApplication().getRepository();
            CoinEntity entity = repository.loadCoinSync(assetItem.getCoinId());
            if (entity != null) {
                entity.setShow(!entity.isShow());
                repository.updateCoin(entity);
            }
        }
    }

    public MutableLiveData<List<AssetItem>> getAssetListFromConfig() {
        if (extraAssetItem != null) {
            return extraAssetItem;
        }
        extraAssetItem = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            extraAssetItem.postValue(CoinConfigHelper.getExtraCoins());
        });
        return extraAssetItem;
    }

    public LiveData<List<AssetItem>> getAssetListFromDB() {
        DataRepository repository = MainApplication.getApplication().getRepository();
        return Transformations.map(repository.loadCoins(), input -> input.stream()
                .filter(this::filterSomeEntity)
                .peek(this::convert)
                .map(AssetItem::new)
                .collect(Collectors.toList()));
    }

    private boolean filterSomeEntity(CoinEntity coinEntity) {
        String coinCode = coinEntity.getCoinCode();
        switch (coinCode) {
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
            case "BTC_CORE_WALLET":
            case "CFX":
            case "DCR":
            case "FIRO":
            case "IOST":
            case "EOS":
            case "XTN":
                return false;
            default:
                return true;
        }
    }

    private void convert(CoinEntity coinEntity) {
        if (coinEntity.getCoinCode().equals("BTC")) {
            coinEntity.setName("Bitcoin");
        } else if (coinEntity.getCoinCode().equals("TRON")) {
            coinEntity.setCoinCode("TRX");
        } else if (coinEntity.getCoinCode().equals("XRP")) {
            coinEntity.setName("XRP");
        }
    }
}
