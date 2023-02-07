package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.actions.accesskey;

public class FunctionCallPermission extends KeyPermission {

    private String allowance;
    private String receiverId;
    private String methodNames;


    public FunctionCallPermission() {
        permissionType = "Function Call";
    }

    public String getAllowance() {
        return allowance;
    }

    public void setAllowance(String allowance) {
        this.allowance = allowance;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(String methodNames) {
        this.methodNames = methodNames;
    }

    @Override
    public String toString() {
        return "FunctionCallPermission{" +
                "allowance='" + allowance + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", methodNames='" + methodNames + '\'' +
                ", permissionType='" + permissionType + '\'' +
                '}';
    }
}
