package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BlacklistParameter extends Parameter {
    private byte[] proposalHash;
    private boolean hasRefIndex;
    private long refIndex;

    public BlacklistParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(proposalHash);
        scw.writeBoolean(hasRefIndex);
        if (hasRefIndex) {
            scw.writeUint32(refIndex);
        }
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        proposalHash = scr.readByteArray(32);
        hasRefIndex = scr.readBoolean();
        if (hasRefIndex) {
            refIndex = scr.readUint32();
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject obj = new JSONObject().put("ProposalHash", Hex.toHexString(proposalHash));
        if (hasRefIndex) {
            obj.put("RefIndex", refIndex);
        }
        return obj;
    }
}
