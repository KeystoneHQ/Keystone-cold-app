package com.keystone.coinlib.ens;

import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

public class EnsLoadManager {
    private final String address;
    private static final List<ENSStoreEngine> ENS_STORE_ENGINES;

    static {
        ENS_STORE_ENGINES = Collections.singletonList(new TFCardENSStore());
    }

    public EnsLoadManager(String address) {
        this.address = address.toLowerCase();
    }

    public String load() {
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        for (ENSStoreEngine ensStoreEngine : ENS_STORE_ENGINES) {
            String name = ensStoreEngine.load(address);
            if (!TextUtils.isEmpty(name)) {
                return name;
            }
        }
        return null;
    }
}