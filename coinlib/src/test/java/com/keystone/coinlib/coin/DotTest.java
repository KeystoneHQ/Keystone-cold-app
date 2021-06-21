/*
 *
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coin;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.DOT.Dot;

import org.bitcoinj.core.Base58;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DotTest {

    @Test
    public void testEncodeAddress() {
        String expub = "xpub69XF3sp9ePvENHmDenwg9humjwqtKuZbZ8go4p8qTpQWn1SFzXTT8DhMtPX66T6sPpaZjtdQrB7cqu3mcrn7Dch85GVrc9grt3n3AvC9BVb";
        System.out.println(Hex.toHexString(Base58.decode("16iM7BVPSvuJnjMW5T7rGWv4PTvgybD5sUS1zZyQkEf7DMHY")));
        String addr = new Dot.Deriver().derive(expub);
        assertEquals("1vQAnWwUYeEnoF1yK51ZmHpaVRs6inHHEJhzJto3xgqe4pF", addr);
        System.out.println(AddressCodec.encodeAddress(Hex.decode("b27c9706a72d4642c407cad42d8c6c4d45a736902487c0d3ff602ac14ffa5e31"), (byte) 0));
    }
}
