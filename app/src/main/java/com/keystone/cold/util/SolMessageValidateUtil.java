package com.keystone.cold.util;

import com.keystone.coinlib.coins.SOL.SolImpl;

public class SolMessageValidateUtil {

    public enum DataType {
        TRANSACTION,
        MESSAGE,
        INVALIDATE
    }

    public static DataType judgeDataType(String message) {
        int result = SolImpl.validateMessage(message);
        switch (result) {
            case 0:
                return DataType.MESSAGE;
            case 1:
                return DataType.TRANSACTION;
            default:
                return DataType.INVALIDATE;
        }
    }
}
