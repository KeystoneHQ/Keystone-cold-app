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

package com.keystone.coinlib.coins.polkadot;

public class ChainProperty {

    public final CallId callId;
    public final String genesisHash;
    public final byte addressPrefix;
    public final byte payloadVersion;

    public static ChainProperty of(String coinCode) {
        if (coinCode.equals("DOT")) {
            return Polkadot;
        } else if (coinCode.equals("KSM")) {
            return Kusama;
        }
        return null;
    }

    public ChainProperty(CallId callId,
                         String genesisHash,
                         byte addressPrefix,
                         byte payloadVersion) {
        this.callId = callId;
        this.genesisHash = genesisHash;
        this.addressPrefix = addressPrefix;
        this.payloadVersion = payloadVersion;
    }

    public static ChainProperty Polkadot = new ChainProperty(CallId.POLKADOT,
            "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3", (byte) 0, (byte) 0x84);
    public static ChainProperty Kusama = new ChainProperty(CallId.KUSAMA,
            "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe", (byte) 2, (byte) 0x84);
}
