package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidETHAccountException extends BaseException {
    public InvalidETHAccountException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
