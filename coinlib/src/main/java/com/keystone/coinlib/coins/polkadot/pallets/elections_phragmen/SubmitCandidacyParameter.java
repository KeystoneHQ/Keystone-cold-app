package com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SubmitCandidacyParameter extends Parameter {
    private long candidateCount;
    public SubmitCandidacyParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(candidateCount);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        candidateCount = scr.readCompact().longValue();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("CandidateCount", candidateCount);
    }
}
