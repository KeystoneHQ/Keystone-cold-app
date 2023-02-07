package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions;

public class CreateAccount extends Action {
    public CreateAccount() {
        priority = 1;
    }

    @Override
    public String toString() {
        return "CreateAccount{" +
                "actionType='" + actionType + '\'' +
                '}';
    }
}
