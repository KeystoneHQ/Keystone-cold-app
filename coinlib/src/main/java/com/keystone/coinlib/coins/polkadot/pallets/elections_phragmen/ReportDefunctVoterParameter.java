package com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ReportDefunctVoterParameter extends Parameter {
    private byte[] who;
    private long voteCount;
    private long candidateCount;

    public ReportDefunctVoterParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(who);
        scw.writeLIntCompact(voteCount);
        scw.writeLIntCompact(candidateCount);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        who = scr.readByteArray(32);
        voteCount = scr.readCompact().longValue();
        candidateCount = scr.readCompact().longValue();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Who", AddressCodec.encodeAddress(who, network.SS58Prefix))
                .put("VoteCount", voteCount)
                .put("CandidateCount", candidateCount);
    }
}
