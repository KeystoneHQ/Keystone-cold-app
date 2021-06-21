package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SetValidatorCountParameter extends Parameter {
    private long newValue;

    public SetValidatorCountParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        this.newValue = scr.readCompactInt();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("new", newValue);
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(newValue);
    }
}
