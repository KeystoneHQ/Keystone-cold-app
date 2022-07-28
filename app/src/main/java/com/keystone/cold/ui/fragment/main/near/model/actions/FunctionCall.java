package com.keystone.cold.ui.fragment.main.near.model.actions;

public class FunctionCall extends Action {

    private String args;
    private String deposit;
    private long gas;
    private String method_name;

    public FunctionCall() {
        priority = 3;
        actionType = "Function Call";
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "actionType='" + actionType + '\'' +
                ", args='" + args + '\'' +
                ", deposit='" + deposit + '\'' +
                ", gas='" + gas + '\'' +
                ", method_name='" + method_name + '\'' +
                '}';
    }
}
