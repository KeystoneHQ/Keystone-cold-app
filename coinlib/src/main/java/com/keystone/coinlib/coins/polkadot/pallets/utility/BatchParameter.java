package com.keystone.coinlib.coins.polkadot.pallets.utility;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.pallets.PalletFactory;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchParameter extends Parameter {
    private int length;
    private List<Parameter> parameters;

    public BatchParameter(Network network, String name, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        parameters = new ArrayList<>();
        length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            int code = scr.readUint16BE();
            Pallet<? extends Parameter> pallet = PalletFactory.getPallet(code, network);
            Parameter parameter = pallet.read(scr);
            parameters.add(parameter);
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Length", length);
        object.put("Calls", toJsonArray(parameters));
        return object;
    }

    public JSONArray toJsonArray(List<Parameter> parameters) throws JSONException {
        JSONArray array = new JSONArray();
        for (Parameter parameter : parameters) {
            array.put(parameter.toJSON());
        }
        return array;
    }


    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(length);
        for (Parameter p : parameters) {
            p.writeTo(scw);
        }
    }
}
