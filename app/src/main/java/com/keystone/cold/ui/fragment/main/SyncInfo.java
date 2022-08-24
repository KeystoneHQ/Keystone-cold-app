package com.keystone.cold.ui.fragment.main;

import android.text.TextUtils;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

public class SyncInfo implements Serializable {
    private String coinId;
    private String path;
    private String address;
    private String name;
    private String addition;
    private byte[] publicKey;

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPublicKey() {
        if (publicKey != null) {
            return publicKey;
        }
        if (Coins.APTOS.coinId().equalsIgnoreCase(coinId)) {
            publicKey = getAptosPublicKeyByAddition();
        } else if (Coins.NEAR.coinId().equalsIgnoreCase(coinId)) {
            publicKey = getNearPublicKeyByAddress();
        } else if (Coins.SOL.coinId().equalsIgnoreCase(coinId)) {
            publicKey = getSolPublicKeyByAddress();
        }
        return publicKey;
    }


    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    private byte[] getAptosPublicKeyByAddition() {
        if (addition == null) {
            return null;
        }
        try {
            JSONObject rootJson = new JSONObject(addition);
            JSONObject additionJson = rootJson.getJSONObject("addition");
            String xPub = additionJson.getString("xPub");
            ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
            return extendedPublicKey.getKey();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    private byte[] getSolPublicKeyByAddress() {
        if (!TextUtils.isEmpty(address)) {
            return new B58().decode(address);
        }
        return null;
    }

    private byte[] getNearPublicKeyByAddress() {
        if (!TextUtils.isEmpty(address)) {
            return Hex.decode(address);
        }
        return null;
    }

    public int getPathDepth() {
        if (!path.toUpperCase().startsWith("M/")) {
            path = "M/" + path;
        }
        return path.split("/").length - 1;
    }
}
