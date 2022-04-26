/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Coins {
    public static final Coin BTC = new Coin("bitcoin", "BTC", "Bitcoin Nested Segwit", 0); // AKA P2SH
    public static final Coin BTC_LEGACY = new Coin("bitcoin_legacy", "BTC_LEGACY", "Bitcoin Legacy", 0);
    public static final Coin BTC_NATIVE_SEGWIT = new Coin("bitcoin_native_segwit", "BTC_NATIVE_SEGWIT", "Bitcoin Native Segwit", 0);
    public static final Coin BTC_TESTNET_SEGWIT = new Coin("bitcoin_testnet", "BTC_TESTNET_SEGWIT", "Bitcoin Nested Segwit", 1); // AKA P2SH
    public static final Coin BTC_TESTNET_LEGACY = new Coin("bitcoin_testnet_legacy", "BTC_TESTNET_LEGACY", "Bitcoin Legacy", 1);
    public static final Coin BTC_TESTNET_NATIVE_SEGWIT = new Coin("bitcoin_testnet_native_segwit", "BTC_TESTNET_NATIVE_SEGWIT", "Bitcoin Native Segwit", 1);
    public static final Coin BCH = new Coin("bitcoin_cash", "BCH", "Bitcoin Cash", 145);
    public static final Coin DASH = new Coin("dash", "DASH", "Dash", 5);
    public static final Coin LTC = new Coin("litecoin", "LTC", "Litecoin", 2);
    public static final Coin ETH = new Coin("ethereum", "ETH", "Ethereum", 60,
            CURVE.SECP256K1, new String[]{ETHAccount.BIP44_STANDARD.getPath(), ETHAccount.LEDGER_LEGACY.getPath(), ETHAccount.LEDGER_LIVE.getPath()});
    public static final Coin ETC = new Coin("ethereum_classic", "ETC", "Ethereum Classic", 61);
    public static final Coin CFX = new Coin("conflux_mainnet", "CFX", "Conflux", 503);
    public static final Coin TRON = new Coin("tron", "TRON", "Tron", 195);
    public static final Coin DCR = new Coin("dcr", "DCR", "Dcr", 42);
    public static final Coin FIRO = new Coin("zcoin", "FIRO", "Zcoin", 136);
    public static final Coin XRP = new Coin("ripple", "XRP", "Ripple", 144);
    public static final Coin IOST = new Coin("iost", "IOST", "IOST", 291, CURVE.ED25519, new String[]{""});
    public static final Coin SOL = new Coin("solana", "SOL", "Solana", 501, CURVE.ED25519, new String[]{
            SOLAccount.SOLFLARE_BIP44.getCode(),
            SOLAccount.SOLFLARE_BIP44_ROOT.getCode(),
            SOLAccount.SOLFLARE_BIP44_CHANGE.getCode()});
    public static final Coin EOS = new Coin("eos", "EOS", "EOS", 194);
    public static final Coin XTN = new Coin("xtn", "XTN", "XTN", 1);
    public static final Coin DOT = new Coin("polkadot", "DOT", "Polkadot", 354, CURVE.SR25519,
            new String[]{"//polkadot"});
    public static final Coin KSM = new Coin("kusama", "KSM", "Kusama", 434, CURVE.SR25519,
            new String[]{"//kusama"});

    public static final List<Coin> SUPPORTED_COINS = Arrays.asList(
            BTC,
            BTC_LEGACY,
            BTC_NATIVE_SEGWIT,
            BTC_TESTNET_SEGWIT,
            BTC_TESTNET_LEGACY,
            BTC_TESTNET_NATIVE_SEGWIT,
            ETC,
            ETH,
            BCH,
            DASH,
            LTC,
            TRON,
            DCR,
            FIRO,
            XRP,
            IOST,
            EOS,
            DOT,
            KSM,
            CFX,
            SOL
    );

    public static boolean isCoinSupported(@NonNull String coinCode) {
        return SUPPORTED_COINS.stream().anyMatch(coin -> coin.coinCode.equals(coinCode));
    }

    public static boolean supportMultiSigner(@NonNull String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
            case "XTN":
            case "BCH":
            case "LTC":
            case "DASH":
                return true;
            default:
                return false;
        }
    }

    public static boolean isBTCFamily(@NonNull String coinCode) {
        return isBTCTestnet(coinCode) || isBTCMainnet(coinCode);
    }

    public static boolean isBTCMainnet(@NonNull String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
                return true;
            default:
                return false;
        }
    }

    public static boolean isBTCTestnet(@NonNull String coinCode) {
        switch (coinCode) {
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
                return true;
            default:
                return false;
        }
    }

    public static String coinCodeFromCoinId(String coinId) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinId.equals(c.coinId))
                .findFirst();
        return coin.isPresent() ? coin.get().coinCode : "";
    }

    public static String coinIdFromCoinCode(String coinCode) {
        if (TextUtils.isEmpty(coinCode)) return "";
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinCode.equals(c.coinCode))
                .findFirst();
        return coin.isPresent() ? coin.get().coinId : "";
    }

    public static String coinNameFromCoinCode(String coinCode) {
        if (TextUtils.isEmpty(coinCode)) return "";
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinCode.equals(c.coinCode))
                .findFirst();
        return coin.isPresent() ? coin.get().coinName : "";
    }

    public static CURVE curveFromCoinCode(String coinCode) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinCode.equals(c.coinCode))
                .findFirst();
        return coin.isPresent() ? coin.get().curve : CURVE.SECP256K1;
    }

    public static String coinCodeOfIndex(int coinIndex) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinIndex == c.coinIndex)
                .findFirst();
        return coin.isPresent() ? coin.get().coinCode : "";
    }

    public static String coinNameOfCoinId(String coinId) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinId.equals(c.coinId))
                .findFirst();
        return coin.isPresent() ? coin.get().coinName() : "";
    }

    public static class Coin {
        private final String coinId;
        private final String coinCode;
        private final String coinName;
        private final int coinIndex;
        private final CURVE curve;
        private final String[] accounts;


        public Coin(String coinId, String coinCode, String coinName, int coinIndex) {
            this(coinId, coinCode, coinName, coinIndex, CURVE.SECP256K1, new String[]{});
        }

        public Coin(String coinId, String coinCode, String coinName, int coinIndex, CURVE curve, String[] accounts) {
            this.coinId = coinId;
            this.coinCode = coinCode;
            this.coinName = coinName;
            this.coinIndex = coinIndex;
            this.curve = curve;
            this.accounts = accounts;
        }

        public String
        coinId() {
            return coinId;
        }

        public String coinCode() {
            return coinCode;
        }

        public String coinName() {
            return coinName;
        }

        public int coinIndex() {
            return coinIndex;
        }

        public CURVE curve() {
            return curve;
        }

        public String[] getAccounts() {
            return accounts;
        }
    }

    public enum CURVE {
        ED25519,
        SECP256K1,
        SECP256R1,
        SR25519
    }

    public static int purposeNumber(String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_TESTNET_SEGWIT":
            case "XTN":
            case "LTC":
                return 49;
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_NATIVE_SEGWIT":
                return 84;
            default:
                return 44;
        }
    }

    public static boolean showPublicKey(String coinCode) {
        switch (coinCode) {
            case "EOS":
            case "IOST":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPolkadotFamily(String coinCode) {
        return coinCode.equals(DOT.coinCode) || coinCode.equals(KSM.coinCode);
    }

    public static boolean isSolanaCoin(String coinCode) {
        return coinCode.equals(SOL.coinCode);
    }

    public static boolean isDefaultOpen(String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "ETH":
                return true;
            default:
                return false;
        }
    }

}
