/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.ui.fragment.main.web3;

import android.text.TextUtils;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AbiItemAdapter {

    private String fromAddress;
    private Web3TxViewModel viewModel;

    public AbiItemAdapter(String fromAddress, Web3TxViewModel viewModel) {
        this.fromAddress = fromAddress;
        this.viewModel = viewModel;
    }

    public List<AbiItem> adapt(JSONObject tx) {
        try {
            JSONArray params = tx.getJSONArray("param");
            List<AbiItem> items = new ArrayList<>();
            String method = tx.optString("method");
            if (!TextUtils.isEmpty(method)) {
                items.add(new AbiItem("method", tx.getString("method"), "method"));
            }
            for (int i = 0; i < params.length(); i++) {
                JSONObject param = params.getJSONObject(i);
                String name = param.getString("name");
                String type = param.getString("type");
                Object value = param.get("value");
                if (adaptGnosis(items, name, type, value)) {
                    continue;
                }
                adaptGeneric(items, name, type, value);
            }
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void adaptGeneric(List<AbiItem> items, String name, String type, Object value) throws JSONException {
        if (TextUtils.equals("tuple", type)) {
            JSONObject tupleObject = new JSONObject();
            tupleObject.put("param", addParamsName(value, name));
            items.addAll(adapt(tupleObject));
        } else if (TextUtils.equals("bytes[]", type)) {
            JSONArray jsonArray = new JSONArray(value.toString());
            StringBuilder concatValue = new StringBuilder();
            for (int j = 0; j < jsonArray.length(); j++) {
                concatValue.append(String.format("(%s)", jsonArray.getString(j)));
            }
            items.add(new AbiItem(name, concatValue.toString(), type));
        } else if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            StringBuilder concatValue = new StringBuilder();
            for (int j = 0; j < arr.length(); j++) {
                StringBuilder item;
                if ("address[]".equals(type)) {
                    item = new StringBuilder();
                    String address = arr.getString(j);
                    String ens = viewModel.loadEnsAddress(address);
                    String addressSymbol = viewModel.recognizeAddress(address);
                    address = Eth.Deriver.toChecksumAddress(address);
                    if (!TextUtils.isEmpty(ens)) {
                        item.append(String.format("<%s>\n", ens));
                    }
                    item.append(address);
                    if (addressSymbol != null) {
                        item.append(String.format(" (%s)", addressSymbol));
                    } else {
//                                item += String.format(" [%s]", "Unknown Address");
                    }
                } else {
                    item = new StringBuilder(arr.getString(j));
                }
                concatValue.append(item);
                if (j != arr.length() - 1) {
                    concatValue.append("\n\n");
                }
            }
            items.add(new AbiItem(name, concatValue.toString(), type));
        } else {
            String item = value.toString();
            items.add(new AbiItem(name, item, type));
        }
    }

    private boolean adaptGnosis(List<AbiItem> items, String name, String type, Object value) {
        if (TextUtils.equals("initializer", name)) {
            String item = value.toString().replace(",", ",\n");
            items.add(new AbiItem(name, item, type));
            return true;
        }
        return false;
    }

    private Object addParamsName(Object value, String name) {
        try {
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String nameItem = jsonObject.getString("name");
                    String typeItem = jsonObject.getString("type");
                    Object valueItem = jsonObject.get("value");
                    if (nameItem.startsWith(name)) continue;
                    if (TextUtils.equals("tuple", typeItem)) {
                        JSONObject tupleObject = new JSONObject();
                        tupleObject.put("param", addParamsName(valueItem, nameItem));
                    }
                    jsonObject.put("name", name + "." + nameItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static class AbiItem {
        String key;
        String value;
        String type;

        public AbiItem(String key, String value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }
    }
}
