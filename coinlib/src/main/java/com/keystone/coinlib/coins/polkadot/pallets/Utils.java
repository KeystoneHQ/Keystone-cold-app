package com.keystone.coinlib.coins.polkadot.pallets;

import com.keystone.coinlib.coins.polkadot.UOS.Network;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Utils {
    public static String getReadableBalanceString(Network network, BigInteger amount) {
        return new BigDecimal(amount)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString() + " " + network.coinCode();
    }

    public static String transformConviction(byte conviction){
        switch (conviction) {
            case 0x00:
                return "None";
            case 0x01:
                return "Locked1x";
            case 0x02:
                return "Locked2x";
            case 0x03:
                return "Locked3x";
            case 0x04:
                return "Locked4x";
            case 0x05:
                return "Locked5x";
            case 0x06:
                return "Locked6x";
            default:
                throw new Error("Unknown Conviction");
        }
    }
}
