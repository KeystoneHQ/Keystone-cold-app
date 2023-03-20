package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class UnsupportedTransactionException extends BaseException {
    public UnsupportedTransactionException(String localeMessage, String message) {
        super(localeMessage, message);
    }

    public static UnsupportedTransactionException newInstance(String debugMessage) {
        return new UnsupportedTransactionException(CONTEXT.getString(R.string.unsupported_transaction), debugMessage);
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.unsupported_transaction_title);
    }
}
