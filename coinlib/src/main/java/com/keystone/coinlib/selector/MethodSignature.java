package com.keystone.coinlib.selector;

public class MethodSignature {
    private String signature;
    private String methodName;

    public MethodSignature() {

    }

    public MethodSignature(String signature, String methodName) {
        this.signature = signature;
        this.methodName = methodName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "MethodSignature{" +
                "signature='" + signature + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
