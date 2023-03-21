package com.keystone.cold.remove_wallet_mode.helper;

public class PageStatusHelper {

    private static class Inner {
        private static final PageStatusHelper INSTANCE = new PageStatusHelper();
    }

    private PageStatusHelper() {
    }

    public static PageStatusHelper getInstance() {
        return Inner.INSTANCE;
    }

    private volatile boolean active = false;

    public void front() {
        active = true;
    }

    public void back() {
        active = false;
    }

    public boolean getStatus() {
        return active;
    }


}
