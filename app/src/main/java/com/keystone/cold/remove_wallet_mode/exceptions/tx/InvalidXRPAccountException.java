package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidXRPAccountException extends BaseException {

    public InvalidXRPAccountException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static InvalidXRPAccountException newInstance(String debugMessage) {
        return new InvalidXRPAccountException(CONTEXT.getString(R.string.invalid_xrp_account_title), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
