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

package com.keystone.cold.ui.fragment.main;

import android.text.TextUtils;

import com.keystone.cold.viewmodel.EthTxConfirmViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AbiItemAdapter {

    private String fromAddress;
    private EthTxConfirmViewModel viewModel;
    public AbiItemAdapter(String fromAddress, EthTxConfirmViewModel viewModel) {
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
                if (TextUtils.equals("tuple", type)) {
                    JSONObject tupleObject = new JSONObject();
                    tupleObject.put("param", addParamsName(value));
                    items.addAll(adapt(tupleObject));
                } else if (value instanceof JSONArray) {
                    JSONArray arr = (JSONArray) value;
                    StringBuilder concatValue = new StringBuilder();
                    for (int j = 0; j < arr.length(); j++) {
                        String item = arr.getString(j);
                        if ("address[]".equals(type)) {
                            BigInteger bigInteger = new BigInteger(item);
                            item = "0x" + bigInteger.toString(16);
                            String addressSymbol = viewModel.recognizeAddress(item);
                            if (addressSymbol != null) {
                                item += String.format(" (%s)", addressSymbol);
                            } else {
                                item += String.format(" [%s]", "Unknown Address");
                            }
                        }
                        concatValue.append(item);
                        if (j != arr.length() -1) {
                            concatValue.append("\n");
                        }
                    }
                    items.add(new AbiItem(name, concatValue.toString(),type));
                } else {
                    String item = value.toString();
                    if ("address".equals(type)) {
                        BigInteger bigInteger = new BigInteger(item);
                        item = "0x" + bigInteger.toString(16);
                        String addressSymbol = viewModel.recognizeAddress(item);
                        if (addressSymbol != null) {
                            item += String.format(" (%s)", addressSymbol);
                        } else if (!"to".equals(name)) {
                            item += String.format(" [%s]", "Unknown Address");
                        }
                    }
                    items.add(new AbiItem(name, item, type));
                }
            }
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object addParamsName(Object value) {
        try {
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    jsonObject.put("name", "params." + name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static class AbiItem{
        String key;
        String value;
        String type;

        public AbiItem(String key, String value,String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }
    }
}
