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
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.pallets.PalletFactory;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AsMultiParameter extends Parameter {
    private int threshold;
    private int otherSignatoriesLen;
    private List<byte[]> otherSignatories;
    private boolean hasTimePoint;
    private long height;
    private long index;
    private Parameter call;
    private boolean storeCall;
    private BigInteger maxWeight;

    public AsMultiParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeUint16(threshold);
        scw.writeCompact(otherSignatoriesLen);
        for (int i = 0 ; i < otherSignatoriesLen; i++) {
            writeAccount(scw, otherSignatories.get(i));
        }
        if (hasTimePoint) {
            scw.writeByte(1);
            scw.writeUint32(height);
            scw.writeUint32(index);
        } else {
            scw.writeByte(0);
        }
        call.writeTo(scw);
        scw.writeBoolean(storeCall);
        scw.writeUint64(maxWeight);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        threshold = scr.readUint16();
        otherSignatoriesLen = scr.readCompactInt();
        otherSignatories = new ArrayList<>();
        for (int i = 0; i < otherSignatoriesLen; i++) {
            otherSignatories.add(readAccount(scr));
        }
        hasTimePoint = scr.readBoolean();
        if (hasTimePoint) {
            height = scr.readUint32();
            index = scr.readUint32();
        }
        int code = scr.readUint16BE();
        Pallet<? extends Parameter> pallet = PalletFactory.getPallet(code, network);
        call = pallet.read(scr);
        storeCall = scr.readBoolean();
        maxWeight = scr.readUint64();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("Max_weight", maxWeight.toString(10))
                .put("Call", "0x" + call.toJSON())
                .put("StoreCall", storeCall)
                .put("Maybe_timepoint", formatTimePoint())
                .put("Other_signatories", formatSignatories())
                .put("Threshold", threshold);
    }

    private String formatTimePoint() {
        if (!hasTimePoint) return "";
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
