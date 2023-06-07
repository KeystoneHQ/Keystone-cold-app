package com.keystone.cold.remove_wallet_mode.viewmodel;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.cryptocore.RCCService;
import com.keystone.cold.encryption.EncryptionCoreProvider;
import com.keystone.cold.util.ExtendedPublicKeyCacheHelper;

import java.util.HashMap;
import java.util.Map;

public class ADASetupManager {
    private static ADASetupManager instance;

    public static ADASetupManager getInstance() {
        if (instance == null) {
            instance = new ADASetupManager();
        }
        return instance;
    }

    private final Map<String, String> EXTENDED_PUBLIC_KEY_CACHE = new HashMap<>();

    public static String toPath(int index) {
        return ADARootPath + "/" + index + "'";
    }

    public static final String ADARootPath = "m/1852'/1815'";

    public boolean setupADARootKey(String passphrase, String password) {
        boolean isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
        String portName = EncryptionCoreProvider.getInstance().getPortName();
        RCCService.Passport passport = new RCCService.Passport(password, isMainWallet, portName);
        return RCCService.setupADARootKey(passphrase, passport) != null;
    }

    public boolean preSetupADAKeys(String password) {
        boolean isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
        String portName = EncryptionCoreProvider.getInstance().getPortName();
        RCCService.Passport passport = new RCCService.Passport(password, isMainWallet, portName);
        String[] accounts = Coins.ADA.getAccounts();
        for (String path : accounts) {
            String xpub = RCCService.getADAExtendedPublicKey(path, passport);
            if (xpub == null) {
                // xpub get failed with slip39 wallet
                return false;
            }
            EXTENDED_PUBLIC_KEY_CACHE.put(path, xpub);
        }
        return true;
    }

    public String getXPub(String path) {
        return EXTENDED_PUBLIC_KEY_CACHE.get(path);
    }
}
