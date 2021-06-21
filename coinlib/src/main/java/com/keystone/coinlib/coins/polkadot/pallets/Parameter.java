package com.keystone.coinlib.coins.polkadot.pallets;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import static com.keystone.coinlib.coins.polkadot.pallets.Parameter.AddressType.MULTI_ADDRESS;

public abstract class Parameter {
    protected Network network;
    public String name;
    public int code;
    protected byte[] data;

    public Parameter(String name, Network network, int code, ScaleCodecReader scr) {
        this.network = network;
        this.name = name;
        this.code = code;
        int start = scr.getPos();
        this.read(scr);
        int end = scr.getPos();
        data = Arrays.copyOfRange(scr.getSource(), start, end);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("chain", network.name);
        object.put("parameter", addCallParameter());
        return object;
    }

    protected abstract void write(ScaleCodecWriter scw) throws IOException;

    protected abstract void read(ScaleCodecReader scr);

    protected abstract JSONObject addCallParameter() throws JSONException;

    public void writeTo(ScaleCodecWriter scw) throws IOException {
        scw.writeByte((this.code >> 8) & 0xff);
        scw.writeByte(this.code & 0xff);
        write(scw);
    }

    public static byte[] readAccount(ScaleCodecReader reader) {
        byte value = reader.readByte();
        return reader.readByteArray(32);
    }

    public static void writeAccount(ScaleCodecWriter scw, byte[] accountPublicKey) throws IOException {
        scw.writeByte(MULTI_ADDRESS.getValue());
        scw.writeByteArray(accountPublicKey);
    }

    enum AddressType {
        MULTI_ADDRESS((byte) 0);
        private final byte value;
        AddressType(byte i) {
            this.value = i;
        }

        public byte getValue() {
            return value;
        }
    }
}
