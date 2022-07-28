package com.keystone.cold.ui.fragment.main.near.model.actions;


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
