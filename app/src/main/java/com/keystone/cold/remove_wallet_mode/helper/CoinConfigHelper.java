package com.keystone.cold.remove_wallet_mode.helper;

import com.keystone.coinlib.v8.ScriptLoader;
import com.keystone.cold.MainApplication;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.utils.SharePreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CoinConfigHelper {

    private static final List<String> ORDER_LIST;
    private static final List<String> EVM_ECOLOGY;
    private static final List<String> COSMOS_ECOLOGY;

    static {
        List<String> EVM_ECOLOGY_TEMP;
        List<String> ORDER_LIST_TEMP;
        List<String> COSMOS_ECOLOGY_TEMP;

        try {
            JSONObject coinConfig = new JSONObject(ScriptLoader.readAsset("coin/config.json"));
            ORDER_LIST_TEMP = getDataFromJsonArray(coinConfig.getJSONArray("orderList"));
            EVM_ECOLOGY_TEMP = getDataFromJsonArray(coinConfig.getJSONObject("ecology").getJSONArray("evm"));
            COSMOS_ECOLOGY_TEMP = getDataFromJsonArray(coinConfig.getJSONObject("ecology").getJSONArray("cosmos"));

            int version = coinConfig.getInt("version");
            if (version > SharePreferencesUtil.getCoinConfigVersion(MainApplication.getApplication())) {
                SharePreferencesUtil.setCoinConfig(MainApplication.getApplication(),
                        mapToLocalConfig(coinConfig.getJSONArray("extraCoins")));
                SharePreferencesUtil.setCoinConfigVersion(MainApplication.getApplication(), version);
            }

        } catch (JSONException exception) {
            exception.printStackTrace();
            ORDER_LIST_TEMP = null;
            EVM_ECOLOGY_TEMP = null;
            COSMOS_ECOLOGY_TEMP = null;
        }

        ORDER_LIST = ORDER_LIST_TEMP;
        EVM_ECOLOGY = EVM_ECOLOGY_TEMP;
        COSMOS_ECOLOGY = COSMOS_ECOLOGY_TEMP;
    }

    private static List<String> getDataFromJsonArray(JSONArray jsonArray) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                result.add(jsonArray.getString(i));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        return result;
    }


    private static String mapToLocalConfig(JSONArray coinConfig) {
        JSONObject localConfig = new JSONObject();
        try {
            for (int i = 0; i < coinConfig.length(); i++) {
                JSONObject item = new JSONObject();
                String coinId = coinConfig.getJSONObject(i).getString("coinId");
                boolean show = coinConfig.getJSONObject(i).getBoolean("show");
                item.put("show", show);
                localConfig.put(coinId, item);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return localConfig.toString();
    }

    public static void sortCoinList(List<AssetItem> assetItems) {
        if (ORDER_LIST == null || assetItems == null) {
            return;
        }
        assetItems.sort(((o1, o2) -> {
            int io1 = ORDER_LIST.indexOf(o1.getCoinCode());
            int io2 = ORDER_LIST.indexOf(o2.getCoinCode());
            if (io1 != -1) {
                io1 = assetItems.size() - io1;
            }
            if (io2 != -1) {
                io2 = assetItems.size() - io2;
            }
            return io2 - io1;
        }));
    }


    public static boolean coinInLocalConfig(String coinId) {
        try {
            JSONObject localConfig = new JSONObject(SharePreferencesUtil.getCoinConfig(MainApplication.getApplication()));
            return localConfig.has(coinId);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static void toggleLocalCoin(String coinId) {
        try {
            JSONObject localConfig = new JSONObject(SharePreferencesUtil.getCoinConfig(MainApplication.getApplication()));
            JSONObject coin = localConfig.getJSONObject(coinId);
            boolean show = localConfig.getJSONObject(coinId).getBoolean("show");
            coin.put("show", !show);
            localConfig.put(coinId, coin);
            SharePreferencesUtil.setCoinConfig(MainApplication.getApplication(), localConfig.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    //Get coins that are not in the database. eg:BNB MATIC OKB HT FTM KLAY OP CELO THETA IOTX ONE ...
    public static List<AssetItem> getExtraCoins() {
        List<AssetItem> assetItems = new ArrayList<>();
        try {
            JSONObject coinConfig = new JSONObject(ScriptLoader.readAsset("coin/config.json"));
            JSONArray extraCoins = coinConfig.getJSONArray("extraCoins");
            JSONObject localConfig = new JSONObject(SharePreferencesUtil.getCoinConfig(MainApplication.getApplication()));
            for (int i = 0; i < extraCoins.length(); i++) {
                AssetItem assetItem = new AssetItem();
                JSONObject coin = extraCoins.getJSONObject(i);
                assetItem.setCoinId(coin.getString("coinId"));
                assetItem.setCoinCode(coin.getString("coinCode"));
                assetItem.setNetwork(coin.getString("network"));
                assetItem.setEcology(getCoinEco(assetItem.getCoinCode()));
                assetItem.setShow(localConfig.getJSONObject(assetItem.getCoinId()).getBoolean("show"));
                assetItems.add(assetItem);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return assetItems;
    }

    public static List<String> getCoinEco(String coinCode) {
        List<String> ecology = new ArrayList<>();
        if (EVM_ECOLOGY != null && EVM_ECOLOGY.contains(coinCode)) {
            ecology.add("EVM");
        }
        if (COSMOS_ECOLOGY != null && COSMOS_ECOLOGY.contains(coinCode)) {
            ecology.add("COSMOS ECO");
        }
        if (!ecology.isEmpty()) {
            return ecology;
        }
        return null;
    }


}
