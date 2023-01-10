package com.keystone.cold.remove_wallet_mode.exceptions;

import static com.keystone.cold.MainApplication.getApplication;

import android.content.Context;

import androidx.annotation.Nullable;

public abstract class BaseException extends Exception {
    protected static final Context CONTEXT = getApplication().getApplicationContext();
    private final String localeMessage;

    public BaseException(String localeMessage, String message) {
        super(message);
        this.localeMessage = localeMessage;
    }

    public abstract String getTitle();

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return localeMessage;
    }
}
