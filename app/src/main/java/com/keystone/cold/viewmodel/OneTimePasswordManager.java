package com.keystone.cold.viewmodel;

public class OneTimePasswordManager {
    private static OneTimePasswordManager instance;

    public static OneTimePasswordManager getInstance() {
        if (instance == null) {
            instance = new OneTimePasswordManager();
        }
        return instance;
    }

    private String passwordHash;

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String useAndDrop() {
        String p = passwordHash;
        this.passwordHash = null;
        return p;
    }
}
