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

package com.keystone.coinlib.coins.ETH;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbiDecoder {
    List<Abi.Entry> savedAbis = new ArrayList<>();
    Map<String, Abi.Entry> methodIDs = new HashMap<>();

    public static class DecodedMethod {
        public String name;
        public List<Param> params;

        public DecodedMethod(String name, List<Param> params) {
            this.name = name;
            this.params = params;
        }

        public JSONObject toJson() {
            JSONObject result = new JSONObject();
            try {
                result.put("method", name);
                JSONArray array = new JSONArray();
                for (Param param : params) {
                    JSONObject parameter = new JSONObject();
                    parameter.put("name", param.name);
                    parameter.put("type", param.type);
                    parameter.put("value", formatValue(param));
                    array.put(parameter);
                }
                result.put("param", array);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        private Object formatValue(Param param) {
            if (param.type instanceof SolidityType.ArrayType) {
                SolidityType elementType = ((SolidityType.ArrayType) param.type).elementType;
                Object[] values = (Object[]) param.value;
                JSONArray array = new JSONArray();
                for (Object o: values) {
                    array.put(o.toString());
                }
                return array;
            }
            return param.value.toString();
        }
    }

    public static class Param {
        public String name;
        public SolidityType type;
        public Object value;

        public Param(String name, SolidityType type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }

	public void addAbi(String json) {
        Abi abi = Abi.fromJson(json);
        for (Abi.Entry entry : abi) {
            if (entry == null) {
                continue;
            }
            if (entry.name != null) {
                byte[] methodSignature = entry.encodeSignature();
                methodIDs.put(Hex.toHexString(methodSignature),entry);
            }
            savedAbis.add(entry);
        }
    }

    public List<Abi.Entry> getAbis() {
        return savedAbis;
    }

    public Map<String, Abi.Entry> getMethodIDs() {
        return methodIDs;
    }

    public DecodedMethod decodeMethod(String data){
        String noPrefix = removePrefix(data);
        if (TextUtils.isEmpty(noPrefix)) {
            return null;
        }
        byte[] bytes = Hex.decode(noPrefix);
        String methodId = noPrefix.substring(0,8);
        Abi.Entry entry = methodIDs.get(methodId);
        if (entry instanceof Abi.Function) {
            List<?> decoded = ((Abi.Function) entry).decode(bytes);
            List<Param> params = new ArrayList<>();
            for (int i = 0; i < decoded.size(); i++) {
                String name = entry.inputs.get(i).name;
                SolidityType type = entry.inputs.get(i).type;
                Object value = decoded.get(i);
                Param param = new Param(name, type, value);
                params.add(param);
            }
            return new DecodedMethod(entry.name, params);
        }
        return null;
    }

    private String removePrefix(String data) {
        if (data.startsWith("0x")) {
            return data.substring(2);
        }
        return data;
    }
}
