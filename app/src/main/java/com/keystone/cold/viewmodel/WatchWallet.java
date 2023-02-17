/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.viewmodel;

import static com.keystone.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;

import android.content.Context;
import android.text.TextUtils;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.scan.QREncoding;

public enum WatchWallet {
    KEYSTONE("keystone"),
    POLKADOT_JS("polkadotJs"),
    CORE_WALLET("coreWallet"),
    BIT_KEEP("bitKeep"),
    XRP_TOOLKIT("xrpToolkit"),
    METAMASK("metamask"),
    SOLANA("solana"),
    NEAR("near"),
    APTOS("aptos"),
    ARConnect("arweave"),
    KEPLR_WALLET("keplrWallet");


    public static final String XRP_TOOLKIT_SIGN_ID = "xrp_toolkit_sign_id";
    public static final String POLKADOT_JS_SIGN_ID = "polkadot_js_sign_id";
    public static final String METAMASK_SIGN_ID = "metamask_sign_id";
    public static final String SOLANA_SIGN_ID = "solana_sign_id";
    public static final String NEAR_SIGN_ID = "near_sign_id";
    public static final String APTOS_SIGN_ID = "aptos_sign_id";
    public static final String CORE_WALLET_SIGN_ID = "core_wallet_sign_id";
    public static final String BIT_KEEP_SIGN_ID = "bit_keep_sign_id";
    public static final String ARWEAVE_SIGN_ID = "arweave_sign_id";
    public static final String KEPLR_WALLET_SIGN_ID = "keplr_wallet_sign_id";
    public static final String PETRA_WALLET_SIGN_ID = "petra_wallet_sign_id";
    public static final String UNKNOWN_WALLET_SIGN_ID = "unknown_wallet_sign_id";

    private final String walletId;

    WatchWallet(String walletId) {
        this.walletId = walletId;
    }

    public static WatchWallet getWatchWallet(Context context) {
        String walletId = Utilities.getPrefs(context)
                .getString(SETTING_CHOOSE_WATCH_WALLET, KEYSTONE.getWalletId());
        return getWatchWalletById(walletId);
    }

    public static WatchWallet getWatchWalletById(String walletId) {
        WatchWallet selectWatchWallet = KEYSTONE;
        for (WatchWallet watchWallet : WatchWallet.values()) {
            if (watchWallet.getWalletId().equals(walletId)) {
                selectWatchWallet = watchWallet;
                break;
            }
        }
        return selectWatchWallet;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName(Context context) {
        String[] wallets = context.getResources().getStringArray(R.array.watch_wallet_list);
        String[] walletValues = context.getResources().getStringArray(R.array.watch_wallet_list_values);
        for (int i = 0; i < wallets.length; i++) {
            if (TextUtils.equals(walletId, walletValues[i])) {
                return wallets[i];
            }
        }
        return wallets[0];
    }

    public QREncoding getQrEncoding() {
        if (this == WatchWallet.POLKADOT_JS) {
            return QREncoding.UOS;
        }
        return QREncoding.UR;
    }

    public Coins.Coin[] getSupportedCoins() {
        switch (this) {
            case KEYSTONE:
                if (FeatureFlags.ENABLE_XTN) {
                    return new Coins.Coin[]{Coins.BTC, Coins.BTC_LEGACY, Coins.BTC_NATIVE_SEGWIT, Coins.BCH, Coins.ETH, Coins.XRP, Coins.TRON, Coins.LTC, Coins.DASH, Coins.DOT, Coins.BTC_TESTNET_LEGACY, Coins.BTC_TESTNET_SEGWIT, Coins.BTC_TESTNET_NATIVE_SEGWIT};
                } else {
                    return new Coins.Coin[]{Coins.BTC, Coins.BTC_LEGACY, Coins.BTC_NATIVE_SEGWIT, Coins.BCH, Coins.ETH, Coins.XRP, Coins.TRON, Coins.LTC, Coins.DASH, Coins.DOT};
                }
            case POLKADOT_JS:
                return new Coins.Coin[]{Coins.DOT, Coins.KSM};
            case XRP_TOOLKIT:
                return new Coins.Coin[]{Coins.XRP};
            case METAMASK:
                return new Coins.Coin[]{Coins.ETH};
            case SOLANA:
                return new Coins.Coin[]{Coins.SOL};
            case NEAR:
                return new Coins.Coin[]{Coins.NEAR};
            case CORE_WALLET:
                return new Coins.Coin[]{Coins.BTC_CORE_WALLET, Coins.ETH};
            case BIT_KEEP:
                return new Coins.Coin[]{Coins.BTC_NATIVE_SEGWIT, Coins.ETH};
            case APTOS:
                return new Coins.Coin[]{Coins.APTOS};
            case KEPLR_WALLET:
                return new Coins.Coin[]{Coins.ATOM, Coins.OSMO, Coins.SCRT, Coins.AKT, Coins.CRO, Coins.IOV, Coins.ROWAN, Coins.CTK, Coins.IRIS, Coins.REGEN, Coins.XPRT, Coins.DVPN, Coins.IXO, Coins.NGM, Coins.BLD, Coins.BOOT, Coins.JUNO, Coins.STARS, Coins.AXL, Coins.SOMM, Coins.UMEE, Coins.GRAV, Coins.TGD, Coins.STRD, Coins.KAVA, Coins.EVMOS};
            case ARConnect:
                return new Coins.Coin[]{Coins.AR};
        }
        return new Coins.Coin[]{};
    }

    public static boolean isSupported(Context context, String coinCode) {
        Coins.Coin[] list = getWatchWallet(context).getSupportedCoins();
        for (Coins.Coin coin : list) {
            if (coin.coinCode().equals(coinCode)) {
                return true;
            }
        }
        return false;
    }

    public String getSignId() {
        switch (this) {
            case POLKADOT_JS:
                return POLKADOT_JS_SIGN_ID;
            case XRP_TOOLKIT:
                return XRP_TOOLKIT_SIGN_ID;
            case METAMASK:
                return METAMASK_SIGN_ID;
            case SOLANA:
                return SOLANA_SIGN_ID;
            case NEAR:
                return NEAR_SIGN_ID;
            case APTOS:
                return APTOS_SIGN_ID;
            case CORE_WALLET:
                return CORE_WALLET_SIGN_ID;
            case BIT_KEEP:
                return BIT_KEEP_SIGN_ID;
            case ARConnect:
                return ARWEAVE_SIGN_ID;
            case KEPLR_WALLET:
                return KEPLR_WALLET_SIGN_ID;
        }
        return null;
    }

    public boolean containsSignId(String signId) {
        switch (this) {
            case APTOS:
                if (APTOS_SIGN_ID.equals(signId) || PETRA_WALLET_SIGN_ID.equals(signId)) {
                    return true;
                }
        }
        return false;
    }
}