package com.keystone.cold.util;

import com.keystone.cold.callables.GetExtendedPublicKeyCallable;

import java.util.HashMap;
import java.util.Map;

public class ExtendedPublicKeyCacheHelper {

    private static class Inner {
        private static final ExtendedPublicKeyCacheHelper INSTANCE = new ExtendedPublicKeyCacheHelper();
    }

    private ExtendedPublicKeyCacheHelper() {
    }

    public static ExtendedPublicKeyCacheHelper getInstance() {
        return Inner.INSTANCE;
    }

    private static final Map<String, String> EXTENDED_PUBLIC_KEY_CACHE = new HashMap<>();

    public void clearCache() {
        EXTENDED_PUBLIC_KEY_CACHE.clear();
    }

    public String getExtendedPublicKey(String pubKeyPath) {
        if (EXTENDED_PUBLIC_KEY_CACHE.containsKey(pubKeyPath)) {
            return EXTENDED_PUBLIC_KEY_CACHE.get(pubKeyPath);
        } else {
            String xPub = new GetExtendedPublicKeyCallable(pubKeyPath).call();
            EXTENDED_PUBLIC_KEY_CACHE.put(pubKeyPath, xPub);
            return xPub;
        }
    }

    public void setExtendedPublicKey(String path, String xpub) {
        EXTENDED_PUBLIC_KEY_CACHE.put(path, xpub);
    }
}
