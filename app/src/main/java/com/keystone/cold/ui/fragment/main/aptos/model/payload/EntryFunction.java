package com.keystone.cold.ui.fragment.main.aptos.model.payload;

import java.util.List;

public class EntryFunction extends PayLoad {
    private String function;
    private List<String> typeArguments;
    private List<String> arguments;


    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public List<String> getTypeArguments() {
        return typeArguments;
    }

    public void setTypeArguments(List<String> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }


    @Override
    public String toString() {
        return "EntryFunction{" +
                "type=" + getType() +
                "function='" + function + '\'' +
                ", typeArguments=" + typeArguments +
                ", arguments=" + arguments +
                '}';
    }

    private String typeArgumentsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String str : typeArguments) {
            sb.append(str).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
        return sb.toString();
    }

    private String argumentsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String str : arguments) {
            sb.append(str).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
        return sb.toString();
    }
}
