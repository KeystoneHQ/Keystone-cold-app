package com.keystone.cold.ui.fragment.main.aptos.model.payload;

public class PayLoad {
    private String type;

    private String rawJson;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    @Override
    public String toString() {
        return "PayLoad{" +
                "type='" + type + '\'' +
                ", rawJson='" + rawJson + '\'' +
                '}';
    }
}
