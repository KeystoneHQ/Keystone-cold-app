package com.keystone.cold.remove_wallet_mode.exceptions;

import com.keystone.cold.R;

public class InvalidStateException extends BaseException{
    public InvalidStateException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
