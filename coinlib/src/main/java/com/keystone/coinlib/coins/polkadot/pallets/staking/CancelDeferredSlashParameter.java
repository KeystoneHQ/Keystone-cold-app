package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CancelDeferredSlashParameter extends Parameter {
    private long eraIndex;
    private int length;
    private List<Long> slashIndices;

    public CancelDeferredSlashParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        eraIndex = scr.readUint32();
        length = scr.readCompactInt();
        slashIndices = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            slashIndices.add(scr.readUint32());
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("EraIndex", eraIndex)
                .put("Length", length)
                .put("SlashIndices", toJSONArray());
    }

    private JSONArray toJSONArray() {
        JSONArray array = new JSONArray();
        for (Long slashIndex : slashIndices
        ) {
            array.put(slashIndex);
        }
        return array;
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeUint32(eraIndex);
        scw.writeCompact(length);
        for (Long slashIndex : slashIndices
        ) {
            scw.writeUint32(slashIndex);
        }
    }
}
