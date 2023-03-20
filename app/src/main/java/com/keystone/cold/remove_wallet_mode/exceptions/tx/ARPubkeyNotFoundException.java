package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class ARPubkeyNotFoundException extends BaseException {
    public ARPubkeyNotFoundException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return null;
    }
}
