package com.keystone.cold.util;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.ui.fragment.main.SyncInfo;
import com.sparrowwallet.hummingbird.registry.CryptoAccount;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.CryptoOutput;
import com.sparrowwallet.hummingbird.registry.PathComponent;
import com.sparrowwallet.hummingbird.registry.CryptoMultiAccounts;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class URRegistryHelper {
    public static final String KEY_NAME = "Keystone";

    public static CryptoHDKey generateCryptoHDKey(String path, int type) {
        return generateCryptoHDKeyWithChildren(path, type, null, null);
    }

    private static CryptoHDKey generateCryptoHDKeyWithChildren(String path, int type, CryptoKeypath children, String note) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        byte[] key = extendedPublicKey.getKey();
        byte[] chainCode = extendedPublicKey.getChainCode();
        CryptoCoinInfo useInfo = new CryptoCoinInfo(type, 0);
        byte[] parentFingerprint = extendedPublicKey.getParentFingerprint();
        CryptoKeypath origin = new CryptoKeypath(getPathComponents(path), masterFingerprint, (int) extendedPublicKey.getDepth());
        return new CryptoHDKey(false, key, chainCode, useInfo, origin, children, parentFingerprint, KEY_NAME, note);
    }

    public static CryptoHDKey generateCryptoHDKeyForLedgerLegacy() {
        CryptoKeypath children = new CryptoKeypath(getPathComponents("M/*"), null);
        return generateCryptoHDKeyWithChildren(ETHAccount.LEDGER_LEGACY.getPath(), ETHAccount.LEDGER_LIVE.getType(), children, "account.ledger_legacy");
    }

    public static CryptoHDKey generateCryptoHDKeyForETHStandard() {
        CryptoKeypath children = new CryptoKeypath(getPathComponents("M/0/*"), null);
        return generateCryptoHDKeyWithChildren(ETHAccount.BIP44_STANDARD.getPath(), ETHAccount.BIP44_STANDARD.getType(), children, "account.standard");
    }

    private static CryptoHDKey generateRawKeyForLedgerLive(String path, String note) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        byte[] key = extendedPublicKey.getKey();
        CryptoKeypath origin = new CryptoKeypath(getPathComponents(path), masterFingerprint, (int) extendedPublicKey.getDepth());
        return new CryptoHDKey(false, key, null, null, origin, null, null, KEY_NAME, note);
    }

    private static CryptoHDKey generateRawKey(SyncInfo syncInfo, byte[] masterFingerprint) {
        CryptoKeypath origin = new CryptoKeypath(getPathComponents(syncInfo.getPath()), masterFingerprint, syncInfo.getPathDepth());
        return new CryptoHDKey(false, syncInfo.getPublicKey(), null, null, origin, null, null, syncInfo.getName(), null);
    }

    public static CryptoMultiAccounts generateCryptoMultiAccounts(List<SyncInfo> syncInfoList) {
        List<CryptoHDKey> cryptoHDKeyList = new ArrayList<>();
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        for (SyncInfo syncInfo : syncInfoList) {
            CryptoHDKey cryptoHDKey = generateRawKey(syncInfo, masterFingerprint);
            cryptoHDKeyList.add(cryptoHDKey);
        }
        return new CryptoMultiAccounts(masterFingerprint, cryptoHDKeyList, KEY_NAME);
    }

    public static List<PathComponent> getPathComponents(String path) {
        List<PathComponent> pathComponents = new ArrayList<>();
        if (path != null) {
            String dest = path.substring(2);
            String[] strings = dest.split("/");
            for (String item : strings) {
                try {
                    if (item.contains("'")) {
                        item = item.replace("'", "");
                        pathComponents.add(item.equals("*") ? new PathComponent(true) :
                                new PathComponent(Integer.parseInt(item), true));
                    } else {
                        pathComponents.add(item.equals("*") ? new PathComponent(false) :
                                new PathComponent(Integer.parseInt(item), false));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return pathComponents;
    }

    public static CryptoAccount generateCryptoAccountForLedgerLive(int start, int end) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        List<CryptoOutput> cryptoOutputs = new ArrayList<>();
        for (int i = start; i < end; i++) {
            CryptoOutput cryptoOutput = new CryptoOutput(new ArrayList<>(), generateRawKeyForLedgerLive(ETHAccount.LEDGER_LIVE.getPath() + "/" + i + "'" + "/0/0", "account.ledger_live"));
            cryptoOutputs.add(cryptoOutput);
        }
        return new CryptoAccount(masterFingerprint, cryptoOutputs);
    }

    public static List<CryptoHDKey> generateCryptoHDKeysForLedgerLive(List<Integer> indexes) {
        List<CryptoHDKey> cryptoHDKeys = new ArrayList<>();
        for (Integer a :
                indexes) {
            CryptoHDKey cryptoHDKey = generateRawKeyForLedgerLive(ETHAccount.LEDGER_LIVE.getPath() + "/" + a + "'" + "/0/0", "account.ledger_live");
            cryptoHDKeys.add(cryptoHDKey);
        }
        return cryptoHDKeys;
    }
}
