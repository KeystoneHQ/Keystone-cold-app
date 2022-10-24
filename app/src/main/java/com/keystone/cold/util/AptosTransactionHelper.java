package com.keystone.cold.util;


public class AptosTransactionHelper {

    private static final String TRANSACTION_PREFIX = "b5e97db07fa0bd0e5598aa3643a9bc6f6693bddc1a9fec9e674a461eaa00b193";

    public enum Type {
        MESSAGE, TRANSACTION
    }
    public static Type judgeDataType(String data) {
        if (data.startsWith(TRANSACTION_PREFIX)) {
            return Type.TRANSACTION;
        }
        return Type.MESSAGE;
    }

    public static String getPureSignData(String data) {
        if (data.startsWith(TRANSACTION_PREFIX)) {
            return data.substring(64);
        }
        return data;
    }
}
