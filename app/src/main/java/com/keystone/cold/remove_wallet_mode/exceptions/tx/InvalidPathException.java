package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidPathException extends BaseException {
    public InvalidPathException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static InvalidPathException newInstance(String debugMessage) {
        return new InvalidPathException(CONTEXT.getString(R.string.unknown_path), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.unknown_path_title);
    }
}
