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
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Coins {
    public static final Coin BTC = new Coin("bitcoin", "BTC", "Bitcoin (Nested Segwit)", 0); // AKA P2SH
    // coreWallet use M/44'/60'/0' to derive bech32 bitcoin addresses.
    public static final Coin BTC_CORE_WALLET = new Coin("bitcoin_core_wallet", "BTC_CORE_WALLET", "Bitcoin (Core Native Segwit)", 60);
    public static final Coin BTC_LEGACY = new Coin("bitcoin_legacy", "BTC_LEGACY", "Bitcoin (Legacy)", 0);
    public static final Coin BTC_NATIVE_SEGWIT = new Coin("bitcoin_native_segwit", "BTC_NATIVE_SEGWIT", "Bitcoin (Native Segwit)", 0);
    public static final Coin BTC_TESTNET_SEGWIT = new Coin("bitcoin_testnet", "BTC_TESTNET_SEGWIT", "Bitcoin (Nested Segwit)", 1); // AKA P2SH
    public static final Coin BTC_TESTNET_LEGACY = new Coin("bitcoin_testnet_legacy", "BTC_TESTNET_LEGACY", "Bitcoin (Legacy)", 1);
    public static final Coin BTC_TESTNET_NATIVE_SEGWIT = new Coin("bitcoin_testnet_native_segwit", "BTC_TESTNET_NATIVE_SEGWIT", "Bitcoin (Native Segwit)", 1);
    public static final Coin BCH = new Coin("bitcoin_cash", "BCH", "Bitcoin Cash", 145);
    public static final Coin DASH = new Coin("dash", "DASH", "Dash", 5);
    public static final Coin LTC = new Coin("litecoin", "LTC", "Litecoin", 2);
    public static final Coin ETH = new Coin("ethereum", "ETH", "Ethereum", 60,
            CURVE.SECP256K1, new String[]{ETHAccount.BIP44_STANDARD.getPath(), ETHAccount.LEDGER_LEGACY.getPath(), ETHAccount.LEDGER_LIVE.getPath()});
    // Since CoreWallet use ETH path for AVAX C-Chain, we treat AVAX as ETH for CoreWallet;
    // So this coin will not generate DB entity;
    public static final Coin AVAX = new Coin("avalanche_c_chain", "AVAX", "Avalanche (C-Chain)", 60);
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

    public static final Coin NEAR = new Coin("near", "NEAR", "Near", 397, CURVE.ED25519, new String[]{
            NEARAccount.MNEMONIC.getCode(),
            NEARAccount.LEDGER.getCode()});

    public static final Coin APTOS = new Coin("aptos", "APT", "Aptos", 637, CURVE.ED25519, new String[]{});
    public static final Coin SUI = new Coin("sui", "SUI", "Sui", 784, CURVE.ED25519, new String[]{});

    public static final Coin AR = new Coin("arweave", "AR", "Arweave", 472, CURVE.RSA, new String[]{"M/44'/472'"});

    //Cosmos chains
    public static final Coin ATOM = new Coin("cosmos_hub", "ATOM", "Cosmos Hub", 118);
    public static final Coin OSMO = new Coin("osmosis", "OSMO", "Osmosis", 118);
    public static final Coin SCRT = new Coin("secret_network", "SCRT", "Secret Network", 529);
    public static final Coin AKT = new Coin("akash", "AKT", "Akash", 118);
    public static final Coin CRO = new Coin("crypto.org", "CRO", "Crypto.org", 394);
    public static final Coin IOV = new Coin("starname", "IOV", "Starname", 234);
    public static final Coin ROWAN = new Coin("sifchain", "ROWAN", "Sifchain", 118);
    public static final Coin CTK = new Coin("certik", "CTK", "Certik", 118);
    public static final Coin IRIS = new Coin("irisnet", "IRIS", "IRISnet", 118);
    public static final Coin REGEN = new Coin("regen", "REGEN", "Regen", 118);
    public static final Coin XPRT = new Coin("persistence", "XPRT", "Persistence", 118);
    public static final Coin DVPN = new Coin("sentinel", "DVPN", "Sentinel", 118);
    public static final Coin IXO = new Coin("ixo", "IXO", "ixo", 118);
    public static final Coin NGM = new Coin("e-money", "NGM", "e-Money", 118);
    public static final Coin BLD = new Coin("agoric", "BLD", "Agoric", 564);
    public static final Coin BOOT = new Coin("bostrom", "BOOT", "Bostrom", 118);
    public static final Coin JUNO = new Coin("juno", "JUNO", "Juno", 118);
    public static final Coin STARS = new Coin("stargaze", "STARS", "Stargaze", 118);
    public static final Coin AXL = new Coin("axelar", "AXL", "Axelar", 118);
    public static final Coin SOMM = new Coin("sommelier", "SOMM", "Sommelier", 118);
    public static final Coin UMEE = new Coin("umee", "UMEE", "Umee", 118);
    public static final Coin GRAV = new Coin("gravity_bridge", "GRAV", "Gravity Bridge", 118);
    public static final Coin TGD = new Coin("tgrade", "TGD", "Tgrade", 118);
    public static final Coin STRD = new Coin("stride", "STRD", "Stride", 118);
    public static final Coin KAVA = new Coin("kava", "KAVA", "Kava", 459);
    public static final Coin EVMOS = new Coin("evmos", "EVMOS", "Evmos", 60);

    public static final Coin ADA = new Coin("cardano", "ADA", "Cardano", 1815, CURVE.BIP32_ED25519, new String[]{
            "m/1852'/1815'/0'",
            "m/1852'/1815'/1'",
            "m/1852'/1815'/2'",
            "m/1852'/1815'/3'",
            "m/1852'/1815'/4'",
            "m/1852'/1815'/5'",
            "m/1852'/1815'/6'",
            "m/1852'/1815'/7'",
            "m/1852'/1815'/8'",
            "m/1852'/1815'/9'",
            "m/1852'/1815'/10'",
            "m/1852'/1815'/11'",
            "m/1852'/1815'/12'",
            "m/1852'/1815'/13'",
            "m/1852'/1815'/14'",
            "m/1852'/1815'/15'",
            "m/1852'/1815'/16'",
            "m/1852'/1815'/17'",
            "m/1852'/1815'/18'",
            "m/1852'/1815'/19'",
            "m/1852'/1815'/20'",
            "m/1852'/1815'/21'",
            "m/1852'/1815'/22'",
            "m/1852'/1815'/23'",
    });


    public static final List<Coin> SUPPORTED_COINS = Arrays.asList(
            BTC,
            BTC_CORE_WALLET,
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
            SOL,
            NEAR,
            APTOS,
            SUI,
            AR,
            ADA,
            ATOM,
            OSMO,
            SCRT,
            AKT,
            CRO,
            IOV,
            ROWAN,
            CTK,
            IRIS,
            REGEN,
            XPRT,
            DVPN,
            IXO,
            NGM,
            BLD,
            BOOT,
            JUNO,
            STARS,
            AXL,
            SOMM,
            UMEE,
            GRAV,
            TGD,
            STRD,
            KAVA,
            EVMOS
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

    public static boolean isUTXOLike(@NonNull String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "LTC":
            case "DASH":
            case "BCH":
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

    public static Coins.Coin coinOfCoinId(String coinId) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinId.equals(c.coinId))
                .findFirst();
        return coin.orElse(null);
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
        BIP32_ED25519,
        SECP256K1,
        SECP256R1,
        SR25519,
        RSA,
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

    public static boolean isAptosCoin(String coinCode) {
        return coinCode.equals(APTOS.coinCode);
    }

    public static boolean isSuiCoin(String coinCode) {
        return coinCode.equals(SUI.coinCode);
    }

    public static boolean isSolanaCoin(String coinCode) {
        return coinCode.equals(SOL.coinCode);
    }

    public static boolean isCosmosFamilyByCoinCode(String coinCode) {
        return coinCode.equals(ATOM.coinCode) || coinCode.equals(OSMO.coinCode) ||
                coinCode.equals(SCRT.coinCode) || coinCode.equals(AKT.coinCode) ||
                coinCode.equals(CRO.coinCode) || coinCode.equals(IOV.coinCode) ||
                coinCode.equals(ROWAN.coinCode) || coinCode.equals(CTK.coinCode) ||
                coinCode.equals(IRIS.coinCode) || coinCode.equals(REGEN.coinCode) ||
                coinCode.equals(XPRT.coinCode) || coinCode.equals(DVPN.coinCode) ||
                coinCode.equals(IXO.coinCode) || coinCode.equals(NGM.coinCode) ||
                coinCode.equals(BLD.coinCode) || coinCode.equals(BOOT.coinCode) ||
                coinCode.equals(JUNO.coinCode) || coinCode.equals(STARS.coinCode) ||
                coinCode.equals(AXL.coinCode) || coinCode.equals(SOMM.coinCode) ||
                coinCode.equals(UMEE.coinCode) || coinCode.equals(GRAV.coinCode) ||
                coinCode.equals(TGD.coinCode) || coinCode.equals(STRD.coinCode) ||
                coinCode.equals(KAVA.coinCode) || coinCode.equals(EVMOS.coinCode);
    }

    public static boolean isCosmosFamilyByCoinId(String coinId) {
        String coinCode = coinCodeFromCoinId(coinId);
        return isCosmosFamilyByCoinCode(coinCode);
    }

    public static String getCosmosCoinId(String chainId) {
        if (TextUtils.isEmpty(chainId)) {
            return Coins.ATOM.coinId();
        }
        if (chainId.startsWith("cosmoshub")) {
            return Coins.ATOM.coinId();
        }
        if (chainId.startsWith("osmosis")) {
            return Coins.OSMO.coinId();
        }
        if (chainId.startsWith("secret")) {
            return Coins.SCRT.coinId();
        }
        if (chainId.startsWith("akashnet")) {
            return Coins.AKT.coinId();
        }
        if (chainId.startsWith("crypto-org-chain")) {
            return Coins.CRO.coinId();
        }
        if (chainId.startsWith("iov")) {
            return Coins.IOV.coinId();
        }
        if (chainId.startsWith("sifchain")) {
            return Coins.ROWAN.coinId();
        }
        if (chainId.startsWith("shentu")) {
            return Coins.CTK.coinId();
        }
        if (chainId.startsWith("irishub")) {
            return Coins.IRIS.coinId();
        }
        if (chainId.startsWith("regen")) {
            return Coins.REGEN.coinId();
        }
        if (chainId.startsWith("core")) {
            return Coins.XPRT.coinId();
        }
        if (chainId.startsWith("sentinelhub")) {
            return Coins.DVPN.coinId();
        }
        if (chainId.startsWith("impacthub")) {
            return Coins.IXO.coinId();
        }
        if (chainId.startsWith("agoric")) {
            return Coins.BLD.coinId();
        }
        if (chainId.startsWith("bostrom")) {
            return Coins.BOOT.coinId();
        }
        if (chainId.startsWith("juno")) {
            return Coins.JUNO.coinId();
        }
        if (chainId.startsWith("stargaze")) {
            return Coins.STARS.coinId();
        }
        if (chainId.startsWith("axelar")) {
            return Coins.AXL.coinId();
        }
        if (chainId.startsWith("sommelier")) {
            return Coins.SOMM.coinId();
        }
        if (chainId.startsWith("umee")) {
            return Coins.UMEE.coinId();
        }
        if (chainId.startsWith("gravity")) {
            return Coins.GRAV.coinId();
        }
        if (chainId.startsWith("tgrade")) {
            return Coins.TGD.coinId();
        }
        if (chainId.startsWith("stride")) {
            return Coins.STRD.coinId();
        }
        if (chainId.startsWith("evmos")) {
            return Coins.EVMOS.coinId();
        }
        return Coins.ATOM.coinId();
    }

    public static String getCosmosCoinCode(String chainId) {
        String coinId = getCosmosCoinId(chainId);
        return coinCodeFromCoinId(coinId);
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
