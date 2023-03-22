package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.util.StringUtils;

public class AddressNameConvertHelper {


    //
    //todo  add other coin convert rule
    public static String convertName(String coinId, String originalName) {
        String result = originalName;

        if (Coins.APTOS.coinId().equals(coinId)) {
            if (originalName.toUpperCase().startsWith("APTOS-")) {
                result = StringUtils.capitalizes(originalName.toLowerCase());
            } else if (originalName.startsWith("APT-")) {
                result = originalName.replace("APT", "Aptos");
            }
        }
        return result;
    }

    public static String convertNameWithAsset(String coinId, String originalName, AssetItem assetItem) {
        String result = originalName;
        if (Coins.ETH.coinId().equals(coinId)) {
            if (originalName.toUpperCase().startsWith("ETH-")) {
                String number = originalName.split("-")[1];
                result = StringUtils.capitalizes(assetItem.getCoinCode() + "-" + number);
            }
        }
        return result;
    }
}
