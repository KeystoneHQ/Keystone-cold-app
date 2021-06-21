package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.pallets.Utils;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class BondParameter extends Parameter {
    private byte[] publicKey;
    private BigInteger amount;
    private Payee payee;

    public BondParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKey = readAccount(scr);
        amount = scr.readCompact();
        payee = Payee.readToPayee(scr);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Controller", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        object.put("Value", Utils.getReadableBalanceString(this.network, this.amount));
        payee.writeToJSON(network, object);
        return object;
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        writeAccount(scw, publicKey);
        scw.writeBIntCompact(amount);
        payee.writeTo(scw);
    }
}
