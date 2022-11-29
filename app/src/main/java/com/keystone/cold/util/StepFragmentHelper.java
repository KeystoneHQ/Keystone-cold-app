package com.keystone.cold.util;

import com.keystone.cold.MainApplication;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.HashMap;
import java.util.Map;

public class StepFragmentHelper {

    private final Map<String, String> container = new HashMap<>();

    private static class Inner {
        private static final StepFragmentHelper INSTANCE = new StepFragmentHelper();
    }
    private StepFragmentHelper(){}

    public static StepFragmentHelper getInstance(){
        return StepFragmentHelper.Inner.INSTANCE;
    }

    public void setStartingPoint(String startingPoint) {
        WatchWallet watchWallet = WatchWallet.getWatchWallet(MainApplication.getApplication());
        container.put(watchWallet.getWalletId(), startingPoint);
    }

    public String getStartingPoint() {
        WatchWallet watchWallet = WatchWallet.getWatchWallet(MainApplication.getApplication());
        return container.remove(watchWallet.getWalletId());
    }
}
