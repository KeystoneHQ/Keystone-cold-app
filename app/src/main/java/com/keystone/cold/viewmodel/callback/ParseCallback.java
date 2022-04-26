package com.keystone.cold.viewmodel.callback;

public interface ParseCallback {
    void OnSuccess(String json);

    void onFailed();
}
