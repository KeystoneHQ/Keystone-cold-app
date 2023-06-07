package com.keystone.cold.remove_wallet_mode.exceptions;

import com.keystone.cold.R;

public class InvalidRequestException extends BaseException {
    public InvalidRequestException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static InvalidRequestException newInstance(String debugMessage) {
        return new InvalidRequestException(CONTEXT.getString(R.string.invalid_key_request), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
