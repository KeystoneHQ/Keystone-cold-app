package com.keystone.cold.ui.fragment.main.cosmos.model.msg;

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
