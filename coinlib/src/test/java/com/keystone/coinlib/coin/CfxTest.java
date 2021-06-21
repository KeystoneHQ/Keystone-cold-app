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

import com.keystone.coinlib.coins.CFX.Cfx;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class CfxTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "0x1a87af7a97b0fdbe20e0876558242080224df8d9",
                "0x1350b1fff9a39d39868ad2686ab377c3386d1e47",
                "0x1715c549840850accfa03d058bda9348eccafba8",
                "0x134f50b7dc9813a242b86be886d855a1205011fe",
                "0x14b4f3cb87a423374c68699cd4d8871b4857373b",
                "0x15910cd35bdf40371963bd0f67014fdd7d31d104",
                "0x178fa053c1c4c1794316b1cd77aa8f74ac9d2aa8",
                "0x1024b67c023238f30fcf2f19992b2181c7f37299",
                "0x1c1add082549c318b46135446d17a8780615fc68",
                "0x1016021afacffea09ef16305575bba3a611326e2",
                "0x1482c0095627945ef8358bec575aa1a408dbb4f2",
                };
        String pubKey = "xpub6BfuP8AaZEqdkesFsnYKR3XrmiLM3ovccFMKnrGoTfL3EY7x2LTDFChFPEY9aixnrgWmLDLJUxQ57dFmiYH2g6PVWPX2boBTQ7PhUu8ARUb";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Cfx.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

    @Test
    public void testCIP37Address() {
        String address = "106d49f8505410eb4e671d51f7d96d2c87807b09";
        assertEquals("cfx:aajg4wt2mbmbb44sp6szd783ry0jtad5bea80xdy7p",
                Cfx.convertToBase32Address(address, 1029));

        String hexAddress = Cfx.convertToHexAddress("cfx:aajg4wt2mbmbb44sp6szd783ry0jtad5bea80xdy7p");
        assertEquals("0x106d49f8505410eb4e671d51f7d96d2c87807b09", hexAddress);


    }

}










