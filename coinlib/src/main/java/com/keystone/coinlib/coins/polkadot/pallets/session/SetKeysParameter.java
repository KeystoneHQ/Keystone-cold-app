package com.keystone.coinlib.coins.polkadot.pallets.session;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetKeysParameter extends Parameter {
    private Map<String,byte[]> publicKeys;
    private byte[] proof;
    private static final String[] keyNames = new String[] {
            "grandpa",
            "babe",
            "im_online",
            "para_validator",
            "para_assignment",
            "authority_discovery",
    };

    public SetKeysParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKeys = new HashMap<>();
        for (String keyName : keyNames) {
            publicKeys.put(keyName, scr.readByteArray(32));
        }
        proof = scr.readByteArray(1);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        List<String> sortedKeys = new ArrayList<>(publicKeys.keySet());
        Collections.sort(sortedKeys);

        return new JSONObject()
                .put("Keys", sortedKeys.stream()
                        .map(s -> s + ":\n" + "0x"+ Hex.toHexString(publicKeys.get(s))+"\n")
                        .reduce((s1, s2) -> s1 + s2)
                        .orElse(""))
                .put("Proof", Hex.toHexString(proof));
    }


    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        for (String key : keyNames) {
            scw.writeByteArray(publicKeys.get(key));
        }
        scw.writeByteArray(proof);
    }
}
