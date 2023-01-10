package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class UnknownQrCodeException extends BaseException {
    public UnknownQrCodeException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return null;
    }
}
