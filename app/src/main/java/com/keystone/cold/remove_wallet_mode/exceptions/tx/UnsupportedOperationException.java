package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class UnsupportedOperationException extends BaseException {
    public UnsupportedOperationException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static UnsupportedOperationException newInstance(String debugMessage) {
        return new UnsupportedOperationException(CONTEXT.getString(R.string.unsupported_operation), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.unsupported_operation_title);
    }
}
