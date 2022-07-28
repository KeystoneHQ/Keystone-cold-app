package com.keystone.cold.ui.fragment.main.near.model.actions;

public class Stake extends Action{

    private String stake;
    private String publicKey;

    public Stake() {
        priority = 5;
        actionType = "Stake";
    }

    public String getStake() {
        return stake;
    }

    public void setStake(String stake) {
        this.stake = stake;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Stake{" +
                "actionType='" + actionType + '\'' +
                ", stake='" + stake + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
