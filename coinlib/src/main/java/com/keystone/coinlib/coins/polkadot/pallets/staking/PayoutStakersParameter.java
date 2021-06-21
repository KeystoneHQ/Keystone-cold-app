package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PayoutStakersParameter extends Parameter {
    private byte[] publicKey;
    private long eraIndex;

    public PayoutStakersParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKey = readAccount(scr);
        eraIndex = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("ValidatorSlash", AddressCodec.encodeAddress(publicKey, this.network.SS58Prefix))
                .put("Era", eraIndex);
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        writeAccount(scw, publicKey);
        scw.writeUint32(eraIndex);
    }
}
