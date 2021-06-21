package com.keystone.coinlib.coins.polkadot.pallets.identity;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SetIdentityParameter extends Parameter {
    private final Map<byte[], byte[]> additional = new HashMap<>();
    private byte[] display;
    private byte[] legal;
    private byte[] web;
    private byte[] riot;
    private byte[] email;
    private byte[] pgpFingerprint;
    private byte[] image;
    private byte[] twitter;

    public SetIdentityParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(data);
    }

    private byte[] readRaw(ScaleCodecReader scr) {
        int rawLength = scr.readByte();
        byte[] content = new byte[0];
        if(rawLength > 1 ){
            content = scr.readByteArray(rawLength - 1);
        }
        return content;
    }

    private void readAdditional(ScaleCodecReader scr) {
        int length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            byte[] key = readRaw(scr);
            byte[] content = readRaw(scr);
            additional.put(key, content);
        }
    }

    private void readPgpFingerprint(ScaleCodecReader scr) {
        boolean isSome = scr.readBoolean();
        if (isSome) {
            pgpFingerprint = scr.readByteArray(20);
        }
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        readAdditional(scr);
        display = readRaw(scr);
        legal = readRaw(scr);
        web = readRaw(scr);
        riot = readRaw(scr);
        email = readRaw(scr);
        readPgpFingerprint(scr);
        image = readRaw(scr);
        twitter = readRaw(scr);
    }

    private String fromHexToUtf8String(byte[] hex) {
        return new String(hex, StandardCharsets.UTF_8);
    }

    private String getAdditional() {
        StringBuilder sb = new StringBuilder();
        additional.forEach((key, value) -> sb.append(fromHexToUtf8String(key))
                .append(":\n")
                .append(fromHexToUtf8String(value))
                .append("\n"));
        return sb.toString();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Additional", getAdditional())
                .put("Display", fromHexToUtf8String(display))
                .put("Legal", fromHexToUtf8String(legal))
                .put("Web", fromHexToUtf8String(web))
                .put("Riot", fromHexToUtf8String(riot))
                .put("Email", fromHexToUtf8String(email))
                .put("Image", fromHexToUtf8String(image))
                .put("PgpFingerprint", pgpFingerprint == null ? "" : Hex.toHexString(pgpFingerprint))
                .put("Twitter", fromHexToUtf8String(twitter));
        return object;
    }
}
