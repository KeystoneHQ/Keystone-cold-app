package com.keystone.cold.ui.fragment.main.near.model.actions;

import androidx.annotation.NonNull;

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
