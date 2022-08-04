package com.keystone.coinlib.exception;

import com.keystone.coinlib.accounts.NEARAccount;

public class InvalidNEARAccountException extends Exception {

    private NEARAccount target;
    private NEARAccount account;

    public NEARAccount getTarget() {
        return target;
    }

    public NEARAccount getAccount() {
        return account;
    }

    public InvalidNEARAccountException(String message, NEARAccount account, NEARAccount target) {
        super(message);
        this.account = account;
        this.target = target;
    }
}