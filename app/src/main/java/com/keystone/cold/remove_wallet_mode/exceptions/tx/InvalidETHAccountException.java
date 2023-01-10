package com.keystone.cold.remove_wallet_mode.exceptions.tx;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;

public class InvalidETHAccountException extends BaseException {
    private ETHAccount current;
    private ETHAccount target;

    public InvalidETHAccountException(String localeMessage, String message, ETHAccount current, ETHAccount target) {
        super(localeMessage, message);
        this.current = current;
        this.target = target;
    }

    public InvalidETHAccountException(String localeMessage, String message) {
        super(localeMessage, message);
        this.current = null;
        this.target = null;
    }

    @Override
    public String getTitle() {
        return CONTEXT.getString(R.string.invalid_data);
    }
}
