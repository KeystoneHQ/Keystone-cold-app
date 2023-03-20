package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class ARPubkeyNotFoundException extends BaseException {
    public ARPubkeyNotFoundException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static ARPubkeyNotFoundException newInstance() {
        return new ARPubkeyNotFoundException("", "cannot get ar public key");
    }

    @Override
    public String getTitle() {
        return null;
    }
}
