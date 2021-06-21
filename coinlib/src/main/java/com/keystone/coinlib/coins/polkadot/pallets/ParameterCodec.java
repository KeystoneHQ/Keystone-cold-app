package com.keystone.coinlib.coins.polkadot.pallets;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public interface ParameterCodec {
    void read(ScaleCodecReader scr);

    void write(ScaleCodecWriter scw) throws IOException;

    JSONObject addCallParameter() throws JSONException;
}
