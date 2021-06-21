package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemoveOtherVoteParameter extends Parameter {
    private byte[] target;
    private long index;

    public RemoveOtherVoteParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        writeAccount(scw, target);
        scw.writeUint32(index);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        target = readAccount(scr);
        index = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Target", AddressCodec.encodeAddress(target, network.SS58Prefix))
                .put("Index", index);
    }
}
