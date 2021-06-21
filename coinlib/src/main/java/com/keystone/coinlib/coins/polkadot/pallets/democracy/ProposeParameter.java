package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.pallets.Utils;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class ProposeParameter extends Parameter {
    private byte[] proposeHash;
    private BigInteger value;

    public ProposeParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(proposeHash);
        scw.writeBIntCompact(value);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        proposeHash = scr.readByteArray(32);
        value = scr.readCompact();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("ProposeHash", Hex.toHexString(proposeHash))
                .put("Value", Utils.getReadableBalanceString(network, value));
    }
}
