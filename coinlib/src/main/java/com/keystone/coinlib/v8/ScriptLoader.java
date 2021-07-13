/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.v8;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.eclipsesource.v8.V8;
import com.keystone.coinlib.Coinlib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScriptLoader {
    private static final String TAG = "ScriptLoader";

    public static ScriptLoader sInstance;

    private ScriptLoader() {
    }

    public static void init() {
        if (sInstance == null) {
            synchronized (ScriptLoader.class) {
                if (sInstance == null) {
                    sInstance = new ScriptLoader();
                }
            }
        }
    }

    public V8 loadByCoinCode(String coinCode) {
        V8 v8 = V8.createV8Runtime("window");
        String js = getJs(coinCode);
        if (!TextUtils.isEmpty(js) && !v8.isReleased()) {
            v8.executeVoidScript(js);
        }
        return v8;
    }

    private String getJs(String coinCode) {
        try {
            JSONObject bundleMap = new JSONObject(readAsset("bundleMap.json"));
            return readAsset("script/" + bundleMap.getString(coinCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readAsset(String fileName) {
        AssetManager am = Coinlib.sInstance.getContext().getAssets();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = am.open(fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line).append("\r\n");
            }
            bf.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
