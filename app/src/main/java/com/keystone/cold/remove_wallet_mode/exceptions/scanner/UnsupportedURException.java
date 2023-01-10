package com.keystone.cold.remove_wallet_mode.exceptions.scanner;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class UnsupportedURException extends BaseException {
    public UnsupportedURException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_qrcode);
    }
}
