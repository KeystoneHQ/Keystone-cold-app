package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidTransactionException extends BaseException {
    public InvalidTransactionException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
