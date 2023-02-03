package com.keystone.cold.remove_wallet_mode.exceptions;

import com.keystone.cold.R;

public class UnknownException extends BaseException {
    public UnknownException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static UnknownException newInstance(Exception e) {
        e.printStackTrace();
        return new UnknownException(null, "unknown exception, please create proper exception type before release. error details: \n" + e.getMessage());
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.unknown_error);
    }
}
