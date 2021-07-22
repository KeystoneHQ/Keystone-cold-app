package com.keystone.cold.util;

import com.keystone.coinlib.accounts.Chains;
import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.PathComponent;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class URRegistryHelper {
    public static CryptoHDKey generateCryptoHDKey(Chains chains) {
        byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
        String xPub = new GetExtendedPublicKeyCallable(chains.getPath()).call();
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        List<PathComponent> pathComponents = getPathComponents(chains);
        byte[] key = extendedPublicKey.getKey();
        byte[] chainCode = extendedPublicKey.getChainCode();
        CryptoCoinInfo useInfo = new CryptoCoinInfo(chains.getType(), 0);
        CryptoKeypath origin = new CryptoKeypath(pathComponents, masterFingerprint, (int) extendedPublicKey.getDepth());
        byte[] parentFingerprint = extendedPublicKey.getParentFingerprint();
        return new CryptoHDKey(false, key, chainCode, useInfo, origin, null, parentFingerprint);
    }

    public static List<PathComponent> getPathComponents(Chains chains) {
        List<PathComponent> pathComponents = new ArrayList<>();
        String dest = chains.getPath();
        if (dest != null) {
            dest = dest.replaceAll("[^0-9']", "");
            String[] strings = dest.split("'");
            for (String string : strings) {
                try {
                    pathComponents.add(new PathComponent(Integer.parseInt(string), true));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return pathComponents;
    }

}
