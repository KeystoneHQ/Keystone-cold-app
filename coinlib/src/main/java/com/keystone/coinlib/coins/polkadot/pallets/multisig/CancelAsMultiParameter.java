/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.polkadot.pallets.multisig;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CancelAsMultiParameter extends Parameter {
    private int threshold;
    private int otherSignatoriesLen;
    private List<byte[]> otherSignatories;
    private byte[] callHash;
    private long height;
    private long index;

    public CancelAsMultiParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeUint16(threshold);
        scw.writeCompact(otherSignatoriesLen);
        for (int i = 0; i < otherSignatoriesLen; i++) {
            writeAccount(scw, otherSignatories.get(i));
        }
        scw.writeUint32(height);
        scw.writeUint32(index);
        scw.writeByteArray(callHash);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        threshold = scr.readUint16();
        otherSignatoriesLen = scr.readCompactInt();
        otherSignatories = new ArrayList<>();
        for (int i = 0; i < otherSignatoriesLen; i++) {
            otherSignatories.add(readAccount(scr));
        }
        height = scr.readUint32();
        index = scr.readUint32();
        callHash = scr.readByteArray(32);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("CallHash", "0x" + Hex.toHexString(callHash))
                .put("Maybe_timepoint", formatTimePoint())
                .put("Other_signatories", formatSignatories())
                .put("Threshold", threshold);
    }

    private String formatTimePoint() {
        return "height: " + height+ "\n"
                +"index: " + index;
    }

    private String formatSignatories() {
        return otherSignatories.stream()
                .map(b -> AddressCodec.encodeAddress(b, network.SS58Prefix))
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("");
    }
}
