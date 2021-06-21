package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NominateParameter extends Parameter {
    private int length;
    private List<byte[]> publicKeys;

    public NominateParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKeys = new ArrayList<>();
        length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            publicKeys.add(readAccount(scr));
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Length", length);
        object.put("Targets", concatAddress());
        return object;
    }

    public String concatAddress() {
        StringBuilder sb = new StringBuilder();
        for (byte[] pubkey : publicKeys) {
            sb.append(AddressCodec.encodeAddress(pubkey, network.SS58Prefix)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(length);
        for (byte[] pk : publicKeys) {
            writeAccount(scw, pk);
        }
    }
}
