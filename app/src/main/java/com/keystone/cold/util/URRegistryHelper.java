package com.keystone.cold.util;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.PathComponent;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class URRegistryHelper {
    private static final String KEY_NAME = "Keystone";

    public static CryptoHDKey generateCryptoHDKey(String path, int type) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        ETHAccount ethAccount = AddAddressViewModel.getETHAccount(path);
        if (path.equals(ETHAccount.BIP44_STANDARD.getPath())) {
            // both ledger legacy and bip44 standard use M/44'/60'/0
            path = ETHAccount.LEDGER_LEGACY.getPath();
        }
        String xPub = new GetExtendedPublicKeyCallable(path).call();
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        byte[] key = extendedPublicKey.getKey();
        byte[] chainCode = extendedPublicKey.getChainCode();
        CryptoCoinInfo useInfo = new CryptoCoinInfo(type, 0);
        byte[] parentFingerprint = extendedPublicKey.getParentFingerprint();
        CryptoKeypath origin = new CryptoKeypath(getPathComponents(path), masterFingerprint, (int) extendedPublicKey.getDepth());
        CryptoKeypath children = null;
        switch (ethAccount) {
            case LEDGER_LIVE:
                children = new CryptoKeypath(getPathComponents(path + "/*'/0/0"), masterFingerprint, (int) extendedPublicKey.getDepth());
                break;
            case LEDGER_LEGACY:
                children = new CryptoKeypath(getPathComponents(path + "/*"), masterFingerprint, (int) extendedPublicKey.getDepth());
                break;
            case BIP44_STANDARD:
                children = new CryptoKeypath(getPathComponents(path + "/0/*"), masterFingerprint, (int) extendedPublicKey.getDepth());
                break;
            default:
                break;
        }
        return new CryptoHDKey(false, key, chainCode, useInfo, origin, children, parentFingerprint, KEY_NAME, "");
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

}
