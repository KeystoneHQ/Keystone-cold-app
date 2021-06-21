package com.keystone.cold.sdcard;

public interface OnSdcardStatusChange {
    String id();
    void onInsert();
    void onRemove();
}
