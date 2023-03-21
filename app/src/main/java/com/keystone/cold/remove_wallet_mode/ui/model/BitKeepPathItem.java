package com.keystone.cold.remove_wallet_mode.ui.model;

import androidx.databinding.ObservableField;

public class BitKeepPathItem {
    private final String code;
    private final String patternName;
    private final String address;
    private final ObservableField<Boolean> isSelect = new ObservableField<>();

    public BitKeepPathItem(String code, String patternName, String address, boolean isSelect) {
        this.code = code;
        this.patternName = patternName;
        this.address = address;
        this.isSelect.set(isSelect);
    }

    public String getCode() {
        return code;
    }

    public String getPatternName() {
        return patternName;
    }

    public ObservableField<Boolean> getIsSelect() {
        return isSelect;
    }

    public String getAddress() {
        return address;
    }
}
