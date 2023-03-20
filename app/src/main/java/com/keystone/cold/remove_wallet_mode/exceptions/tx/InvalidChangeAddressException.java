package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidChangeAddressException extends BaseException {
    public InvalidChangeAddressException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static InvalidChangeAddressException newInstance() {
        return new InvalidChangeAddressException(CONTEXT.getString(R.string.invalid_change_address), "in valid change address");
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_change_address_title);
    }
}
