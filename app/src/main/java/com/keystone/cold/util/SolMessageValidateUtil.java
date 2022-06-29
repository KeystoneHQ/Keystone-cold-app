package com.keystone.cold.util;

import com.keystone.coinlib.coins.SOL.SolImpl;

public class SolMessageValidateUtil {

    public static boolean isTransactionData(String message) {
        return SolImpl.validateMessage(message) == 1;
    }

}
