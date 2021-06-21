package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SecondParameter extends Parameter {
    private long proposal;
    private long secondsUpperBound;

    public SecondParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        proposal = scr.readCompact().longValue();
        secondsUpperBound = scr.readCompact().longValue();
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(proposal);
        scw.writeLIntCompact(secondsUpperBound);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Proposal", proposal).put("SecondsUpperBound", secondsUpperBound);
    }
}
