package com.keystone.cold.remove_wallet_mode.helper;

import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.sync_jump.FewchaWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SolflareWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SyncModeDetector;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

public enum SyncMode {
    INVALID, DIRECT, SELECT_ADDRESS, MULTI_CHAINS, SUBSTRATE;

    public static void detect(String walletId, MutableLiveData<SyncMode> stepMode) {
        Wallet wallet = Wallet.getWalletById(walletId);
        switch (wallet) {
            case BLUEWALLET:
            case METAMASK:
                stepMode.postValue(DIRECT);
                break;
            case POLKADOT:
            case SUBWALLET:
                stepMode.postValue(SUBSTRATE);
                break;
            default:
                complexDetect(wallet, stepMode);
        }
    }

    private static void complexDetect(Wallet wallet, MutableLiveData<SyncMode> stepMode) {
        SyncModeDetector syncModeDetector = getSyncModeDetector(wallet);
        if (syncModeDetector != null) {
            syncModeDetector.detect(new SyncModeDetector.Callback() {
                @Override
                public void useDirect() {
                    stepMode.postValue(DIRECT);
                }

                @Override
                public void useSelectAddress() {
                    stepMode.postValue(SELECT_ADDRESS);
                }

                @Override
                public void invalid() {
                    stepMode.postValue(INVALID);
                }
            });
        } else {
            stepMode.postValue(INVALID);
        }
    }

    private static SyncModeDetector getSyncModeDetector(Wallet wallet) {
        //todo  Need to add other types
        switch (wallet) {
            case FEWCHA:
                return new FewchaWalletSyncModeDetector();
            case SOLFLARE:
                return new SolflareWalletSyncModeDetector();
            default:
                return null;
        }
    }
}
