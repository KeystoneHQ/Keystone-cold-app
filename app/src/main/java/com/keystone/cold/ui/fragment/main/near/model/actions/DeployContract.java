package com.keystone.cold.ui.fragment.main.near.model.actions;

public class DeployContract extends Action{

    public DeployContract() {
        priority = 2;
        actionType = "Delpoy Contract";
    }

    @Override
    public String toString() {
        return "DeployContract{" +
                "actionType='" + actionType + '\'' +
                '}';
    }
}
