package com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RenounceCandidacyParameter extends Parameter {
    private byte renouncingType;
    private long candidateCount;
    public RenounceCandidacyParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByte(renouncingType);
        if(renouncingType == 0x02) {
            scw.writeLIntCompact(candidateCount);
        }
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        renouncingType = scr.readByte();
        if (renouncingType == 0x02){
            candidateCount = scr.readCompact().longValue();
        }
    }

    private String getRenouncingType() {
        switch (renouncingType){
            case 0x00:
                return "Member";
            case 0x01:
                return "RunnerUp";
            case 0x02:
                return "Candidate";
            default:
                throw new Error("Unknown renouncing type");
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("RenouncingType", getRenouncingType());
        if(renouncingType == 0x02) {
            object.put("CandidateCount", candidateCount);
        }
        return object;
    }
}
