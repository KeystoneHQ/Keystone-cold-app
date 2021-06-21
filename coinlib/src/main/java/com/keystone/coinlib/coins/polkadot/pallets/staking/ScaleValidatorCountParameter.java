package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ScaleValidatorCountParameter extends Parameter {
    // TODO: need to check with true extrinsic
    private long percent; // base 100

    public ScaleValidatorCountParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        percent = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Factor", percent);
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeUint32(percent);
    }
}
