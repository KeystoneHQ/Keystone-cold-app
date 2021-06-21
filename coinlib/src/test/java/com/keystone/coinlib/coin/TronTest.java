/*
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
 */

package com.keystone.coinlib.coin;

import com.keystone.coinlib.coins.TRON.Tron;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class TronTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "TYgXziK4zH4T5dchXFBseTFUpZ8kty6oQy",
                "TTgPTW6oaxmRnKz1xFT2qSHx3QNEpP2jZ6",
                "TWD5uP9gDo9QJcYhvoN4u4jS4b9pHV25ow",
                "TTrMEb1R2iUba67yygVVzu8ny6F1R3NbaE",
                "TAGkxJfAd5Xy4D6PL76HAH4WM9W7VVpCDf",
                "TGAnfDpv8GhTmi2WXbvQG7LPGguX5cuMcG",
                "TXSEwZGP5JDZmU9z75SHBMY1GsB7HCjWyD",
                "TB2ibqfKPNxHEqQHHmVUPfVgNGEtXkJ8NL",
                "TSbt9gkbJriTUXmUxgVBLxNFwWwx6e4qTw",
                "TNLvDnx9jECDPTRq31KaxPkMX4zaMLQeVC",
                "TSzi8byHgnEbiHo8h4X5rkcmFXxM3wpZqH",
        };
        String pubKey = "xpub6BmQYH48mM1hm8B2TDXg7oDfa7dZfHVgi7pWFMsmSuJ3rVwi2YEgjZMN1cBFQtieHPgmoY3YQx5G85XyxMvppPxJToxmWjo64gaNutTn6iw";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Tron.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










