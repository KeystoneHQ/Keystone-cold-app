package com.keystone.coinlib.exception;

import com.keystone.coinlib.accounts.SOLAccount;

public class InvalidSOLAccountException extends Exception {

    private SOLAccount target;
    private SOLAccount account;

    public SOLAccount getTarget() {
        return target;
    }

    public SOLAccount getAccount() {
        return account;
    }

    public InvalidSOLAccountException(String message, SOLAccount account, SOLAccount target) {
        super(message);
        this.account = account;
        this.target = target;
    }
}
