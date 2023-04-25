package com.keystone.cold.remove_wallet_mode.helper;

import static com.keystone.coinlib.v8.ScriptLoader.readAsset;
import static com.keystone.cold.remove_wallet_mode.ui.model.AssetItem.TEXT_ECOLOGY_COSMOS;
import static com.keystone.cold.remove_wallet_mode.ui.model.AssetItem.TEXT_ECOLOGY_EVM;

import com.keystone.coinlib.v8.ScriptLoader;
import com.keystone.cold.MainApplication;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.utils.SharePreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                initCoinConfig();
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

    public static void initCoinConfig() {
        try {
            JSONObject coinConfig = new JSONObject(ScriptLoader.readAsset("coin/config.json"));
            int version = coinConfig.getInt("version");
            //TODO load local config when version upgrade
            SharePreferencesUtil.setCoinConfig(MainApplication.getApplication(),
                    mapToLocalConfig(coinConfig.getJSONArray("extraCoins")));
            SharePreferencesUtil.setCoinConfigVersion(MainApplication.getApplication(), version);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
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

    public static List<AssetItem> getEVMChains() {
        List<AssetItem> extraCoins = getExtraCoins();
        return extraCoins.stream().filter(v -> v.getEcology().contains(TEXT_ECOLOGY_EVM)).collect(Collectors.toList());
    }

    public static AssetItem getEVMChainByChainID(long chainId) {
        try {
            JSONObject chainIdJSONObject = new JSONObject(readAsset("chain/chainId.json"));
            JSONObject chain = chainIdJSONObject.optJSONObject(String.valueOf(chainId));
            if (chain == null) return null;
            String coinCode = chain.optString("coinCode", "");
            if (coinCode.isEmpty()) return null;
            List<AssetItem> assetItems = getEVMChains();
            Optional<AssetItem> result = assetItems.stream().filter(v -> v.getCoinCode().equals(coinCode)).findFirst();
            return result.orElse(null);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNonEVMChainIconCode(long chainId) {
        try {
            JSONObject chainIdJSONObject = new JSONObject(readAsset("chain/chainId.json"));
            JSONObject chain = chainIdJSONObject.optJSONObject(String.valueOf(chainId));
            if (chain == null) return null;
            String coinCode = chain.optString("coinCode", "");
            if (coinCode.isEmpty()) return null;
            return coinCode;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getCoinEco(String coinCode) {
        List<String> ecology = new ArrayList<>();
        if (EVM_ECOLOGY != null && EVM_ECOLOGY.contains(coinCode)) {
            ecology.add(TEXT_ECOLOGY_EVM);
        }
        if (COSMOS_ECOLOGY != null && COSMOS_ECOLOGY.contains(coinCode)) {
            ecology.add(TEXT_ECOLOGY_COSMOS);
        }
        if (!ecology.isEmpty()) {
            return ecology;
        }
        return null;
    }


}
