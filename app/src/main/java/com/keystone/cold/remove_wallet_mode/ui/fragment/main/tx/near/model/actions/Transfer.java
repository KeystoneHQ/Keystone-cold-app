package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions;


public class Transfer extends Action {

    private String deposit;

    public Transfer() {
        priority = 4;
        actionType = "Transfer";
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "actionType='" + actionType + '\'' +
                ", deposit='" + deposit + '\'' +
                '}';
    }
}
