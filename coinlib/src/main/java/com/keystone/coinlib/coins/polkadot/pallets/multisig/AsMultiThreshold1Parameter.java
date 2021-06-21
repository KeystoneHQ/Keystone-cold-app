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
import java.util.ArrayList;
import java.util.List;

public class AsMultiThreshold1Parameter extends Parameter {

    private int otherSignatoriesLen;
    private List<byte[]> otherSignatories;
    private Parameter call;


    public AsMultiThreshold1Parameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(otherSignatoriesLen);
        for (int i = 0 ; i < otherSignatoriesLen; i++) {
            writeAccount(scw, otherSignatories.get(i));
        }
        call.writeTo(scw);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        otherSignatoriesLen = scr.readCompactInt();
        otherSignatories = new ArrayList<>();
        for (int i = 0; i < otherSignatoriesLen; i++) {
            otherSignatories.add(readAccount(scr));
        }
        int code = scr.readUint16BE();
        Pallet<? extends Parameter> pallet = PalletFactory.getPallet(code, network);
        call = pallet.read(scr);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("call", call.toJSON())
                .put("OtherSignatories", formatSignatories());
    }

    private String formatSignatories() {
        return otherSignatories.stream()
                .map(b -> AddressCodec.encodeAddress(b, network.SS58Prefix))
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("");
    }
}
