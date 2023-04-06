package com.keystone.cold.ui.fragment.main.aptos.model;


import com.keystone.cold.ui.fragment.main.aptos.model.payload.EntryFunction;
import com.keystone.cold.ui.fragment.main.aptos.model.payload.ModuleBundle;
import com.keystone.cold.ui.fragment.main.aptos.model.payload.PayLoad;
import com.keystone.cold.ui.fragment.main.aptos.model.payload.Script;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AptosTxParser {

    private static final String JOINER = "::";

    public static AptosTx parseAptosExploreFormat(String json) {
        String convertResult = null;
        try {
            convertResult = convertAptosExploreFormat(json);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return parse(convertResult);
    }

    public static AptosTx parse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            long chainId = root.optLong("chain_id");
            long sequenceNumber = root.optLong("sequence_number");
            String sender = root.optString("sender");
            long gasUnitPrice = root.optLong("gas_unit_price");
            long maxGasAmount = root.optLong("max_gas_amount");
            long expirationTimestampSecs = root.optLong("expiration_timestamp_secs");
            PayLoad payload = getPayLoad(root.optJSONObject("payload"));
            AptosTx aptosTx;
            if (isSystemTransfer(payload)) {
                aptosTx = new AptosTransferTx();
            } else {
                aptosTx = new AptosTx();
            }
            aptosTx.setChainId(chainId);
            aptosTx.setSequenceNumber(sequenceNumber);
            aptosTx.setSender(sender);
            aptosTx.setGasUnitPrice(gasUnitPrice);
            aptosTx.setMaxGasAmount(maxGasAmount);
            aptosTx.setExpirationTimestampSecs(expirationTimestampSecs);
            aptosTx.setPayLoad(payload);
            return aptosTx;
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static boolean isSystemTransfer(PayLoad payLoad) {
        if (payLoad instanceof EntryFunction) {
            EntryFunction entryFunction = (EntryFunction) payLoad;
            String function = entryFunction.getFunction();
            if (entryFunction.getTypeArguments() != null && entryFunction.getTypeArguments().size() > 0) {
                String typeArgument = entryFunction.getTypeArguments().get(0);
                if ("0x1::aptos_coin::AptosCoin".equals(typeArgument)) {
                    String[] splitFunction = function.split(JOINER);
                    return splitFunction.length == 3 && "0x1".equals(splitFunction[0]) && "transfer".equals(splitFunction[2]);
                }
            } else {
                return "0x1::aptos_account::transfer".equals(function);
            }
        }
        return false;
    }

    private static PayLoad getPayLoad(JSONObject payload) throws JSONException {
        if (payload == null) {
            return null;
        }
        String type = payload.optString("type");
        switch (type) {
            case "entry_function_payload":
                String function = payload.optString("function");
                JSONArray typeArguments = payload.optJSONArray("typeArguments");
                List<String> typeArgList = new ArrayList<>();
                for (int i = 0; i < typeArguments.length(); i++) {
                    typeArgList.add(typeArguments.getString(i));
                }
                JSONArray arguments = payload.optJSONArray("arguments");
                List<String> argumentList = new ArrayList<>();
                for (int i = 0; i < arguments.length(); i++) {
                    argumentList.add(arguments.getString(i));
                }
                EntryFunction entryFunction = new EntryFunction();
                entryFunction.setRawJson(payload.toString());
                entryFunction.setType("entry_function_payload");
                entryFunction.setFunction(function);
                entryFunction.setTypeArguments(typeArgList);
                entryFunction.setArguments(argumentList);
                return entryFunction;
            case "script_payload":
                Script script = new Script();
                script.setRawJson(payload.toString());
                script.setType("script_payload");
                return script;
            case "module_bundle_payload":
                ModuleBundle moduleBundle = new ModuleBundle();
                moduleBundle.setRawJson(payload.toString());
                moduleBundle.setType("module_bundle_payload");
                return moduleBundle;

        }
        return null;
    }

    public static String convertAptosExploreFormat(String raw) throws JSONException {
        JSONObject rawJson = new JSONObject(raw);
        JSONObject result = new JSONObject();
        Iterator<String> iterator = rawJson.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!"payload".equals(key)) {
                result.put(key, rawJson.get(key));
            } else {
                result.put("payload", convertPayload(rawJson.getJSONObject("payload")));
            }
        }
        return result.toString();
    }


    private static JSONObject convertPayload(JSONObject payload) throws JSONException {
        JSONObject result = new JSONObject();
        Iterator<String> iterator = payload.keys();
        while (iterator.hasNext()) {
            String type = null;
            String key = iterator.next();
            switch (key) {
                case "EntryFunction":
                    type = "entry_function_payload";
                    String function = convertFunction(payload);
                    JSONArray typeArguments = convertTypeArguments(payload.getJSONObject(key).getJSONArray("ty_args"));
                    JSONArray arguments = convertArguments(payload.getJSONObject(key).getJSONArray("args"), null);
                    result.put("function", function);
                    result.put("typeArguments", typeArguments);
                    result.put("arguments", arguments);
                    result.put("type", type);
                    break;
                case "Script":
                    type = "script_payload";
                    result = payload.getJSONObject(key);
                    result.put("type", type);
                    break;
                case "ModuleBundle":
                    type = "module_bundle_payload";
                    result = payload.getJSONObject(key);
                    result.put("type", type);
                    break;
            }

        }
        return result;
    }


    private static String convertFunction(JSONObject payload) throws JSONException {
        JSONObject module = payload.getJSONObject("EntryFunction").getJSONObject("module");
        String address = module.getString("address");
        String name = module.getString("name");
        String function = payload.getJSONObject("EntryFunction").getString("function");
        if ("0000000000000000000000000000000000000000000000000000000000000001".equals(address)) {
            address = "0x1";
        }
        return address + JOINER + name + JOINER + function;
    }


    private static JSONArray convertTypeArguments(JSONArray typeArguments) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        if (typeArguments == null || typeArguments.length() == 0) {
            return jsonArray;
        }
        int num = typeArguments.length();
        for (int i = 0; i < num; i++) {
            Object object = typeArguments.get(i);
            if (object instanceof JSONObject) {
                String typeArgument = convertTypeArgument(typeArguments.getJSONObject(i));
                jsonArray.put(typeArgument);
            } else {
                jsonArray.put(object.toString());
            }
        }
        return jsonArray;
    }

    private static String convertTypeArgument(JSONObject typeArgument) throws JSONException {
        Iterator<String> iterator = typeArgument.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            switch (key) {
                case "struct":
                    JSONObject struct = typeArgument.getJSONObject("struct");
                    String address = struct.getString("address");
                    if ("0000000000000000000000000000000000000000000000000000000000000001".equals(address)) {
                        address = "0x1";
                    }
                    String module = struct.getString("module");
                    String name = struct.getString("name");
                    return address + JOINER + module + JOINER + name;
                case "vector":
                    return convertTypeArgument(typeArgument.getJSONObject("vector"));
                default:
                    return key;
            }
        }
        return null;
    }

    private static JSONArray convertArguments(JSONArray arguments, JSONArray types) throws JSONException {
        JSONArray result = new JSONArray();
        if (types != null) {
            return result;
        }
        int length = arguments.length();
        for (int i = 0; i < length; i++) {
            JSONArray arg = arguments.getJSONArray(i);
            switch (arg.length()) {
                case 8:
                    String u64 = "" + bytesToLongBig(convertToByteArray(arg));
                    result.put(u64);
                    break;
                case 32:
                    String address = "0x" + convertToHex(arg);
                    result.put(address);
                    break;
                default:
                    String defaultValue = convertToHex(arg);
                    result.put(defaultValue);
                    break;
            }
        }
        return result;
    }

    private static String convertToHex(JSONArray jsonArray) throws JSONException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            int value = jsonArray.getInt(i);
            String hex = Integer.toHexString(value);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static byte[] convertToByteArray(JSONArray jsonArray) throws JSONException {
        byte[] data = new byte[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            int value = jsonArray.getInt(i);
            data[data.length - i - 1] = (byte) (value & 0xff);
        }
        return data;
    }


    private static long bytesToLongBig(byte[] array) {
        return ((((long) array[0] & 0xff) << 56)
                | (((long) array[1] & 0xff) << 48)
                | (((long) array[2] & 0xff) << 40)
                | (((long) array[3] & 0xff) << 32)
                | (((long) array[4] & 0xff) << 24)
                | (((long) array[5] & 0xff) << 16)
                | (((long) array[6] & 0xff) << 8)
                | (((long) array[7] & 0xff)));
    }
}
