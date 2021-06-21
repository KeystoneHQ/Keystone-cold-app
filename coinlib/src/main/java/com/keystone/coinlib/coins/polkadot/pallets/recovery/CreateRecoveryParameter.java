package com.keystone.coinlib.coins.polkadot.pallets.recovery;

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

public class CreateRecoveryParameter extends Parameter {
    private List<byte[]> friends;
    private int len;
    private int threshold;
    private long delay_period;

    public CreateRecoveryParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        len = scr.readCompactInt();
        friends = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            friends.add(scr.readByteArray(32));
        }
        threshold = scr.readUint16();
        delay_period = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("Friends", formatFriends())
                .put("Threshold", threshold)
                .put("Delay_period", delay_period);
    }

    private String formatFriends() {
        return friends.stream()
                .map(f -> AddressCodec.encodeAddress(f, network.SS58Prefix))
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("");
    }


    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(len);
        for (int i = 0; i < len; i++) {
            scw.writeByteArray(friends.get(i));
        }
        scw.writeUint16(threshold);
        scw.writeUint32(delay_period);
    }
}
