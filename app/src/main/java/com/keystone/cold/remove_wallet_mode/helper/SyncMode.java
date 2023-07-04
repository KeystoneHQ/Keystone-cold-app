package com.keystone.cold.remove_wallet_mode.helper;

import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.remove_wallet_mode.helper.sync_jump.PetraWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SenderWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SolflareWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SuietWalletSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.SyncModeDetector;
import com.keystone.cold.remove_wallet_mode.helper.sync_jump.XRPToolKitSyncModeDetector;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

public enum SyncMode {
    INVALID, DIRECT, SELECT_ADDRESS, SELECT_COINS, SUBSTRATE, SELECT_ONE_ADDRESS, KEY_REQUEST;

    public static void detect(String walletId, MutableLiveData<SyncMode> stepMode) {
        Wallet wallet = Wallet.getWalletById(walletId);
        switch (wallet) {
            case BLUEWALLET:
            case RABBY:
            case SAFE:
            case ZAPPER:
            case YEARN:
            case SUSHISWAP:
            case BLOCKWALLET:
            case METAMASK:
            case ARCONNECT:
            case KEPLR:
            case CORE:
            case OKX:
            case BITKEEP:
                stepMode.postValue(DIRECT);
                break;
            case POLKADOTJS:
            case FEWCHA:
            case SUBWALLET:
                stepMode.postValue(SUBSTRATE);
                break;
            case KEYSTONE:
                stepMode.postValue(SELECT_COINS);
                break;
            case ETERNL:
                stepMode.postValue(KEY_REQUEST);
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
                public void useSelectOneAddress() {
                    stepMode.postValue(SELECT_ONE_ADDRESS);
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
            case SUIET:
                return new SuietWalletSyncModeDetector();
            case PETRA:
                return new PetraWalletSyncModeDetector();
            case SOLFLARE:
                return new SolflareWalletSyncModeDetector();
            case SENDER:
                return new SenderWalletSyncModeDetector();
            case XRPTOOLKIT:
                return new XRPToolKitSyncModeDetector();
            default:
                return null;
        }
    }
}
