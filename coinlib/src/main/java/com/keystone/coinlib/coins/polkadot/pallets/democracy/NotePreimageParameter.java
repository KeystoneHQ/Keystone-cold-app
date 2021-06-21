package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NotePreimageParameter extends Parameter {
    private int length;
    private byte[] encodedProposal;

    public NotePreimageParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(length);
        scw.writeByteArray(encodedProposal);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        length = scr.readCompactInt();
        encodedProposal = scr.readByteArray(length);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("EncodedProposal", "0x" + Hex.toHexString(encodedProposal));
    }
}
