package com.keystone.cold.ui.fragment.main.near.model.actions;

public class DeleteAccount extends Action {
    private String beneficiaryId;

    public DeleteAccount() {
        priority = 8;
        actionType = "Delete Account";
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    @Override
    public String toString() {
        return "DeleteAccount{" +
                "actionType='" + actionType + '\'' +
                ", beneficiaryId='" + beneficiaryId + '\'' +
                '}';
    }
}
