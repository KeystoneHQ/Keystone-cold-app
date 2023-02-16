package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

public class UnsupportMsg extends Msg {

    private String value;
    public UnsupportMsg(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "UnsupportMsg{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
