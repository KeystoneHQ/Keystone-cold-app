package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class ADAInvalidStateException extends BaseException {
    public ADAInvalidStateException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static ADAInvalidStateException newInstance(String debugMessage) {
        return new ADAInvalidStateException(CONTEXT.getString(R.string.invalid_ada_state), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
