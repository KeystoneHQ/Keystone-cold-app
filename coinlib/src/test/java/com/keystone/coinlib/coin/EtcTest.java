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

import com.keystone.coinlib.coins.ETC.Etc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class EtcTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "0xff1e8f0F67b9C9DEA103ca988831DD9C54acA876",
                "0x95662DfB520aaBE3B3E550675F90Ea05d52ecB5D",
                "0x50a577fdd4ac9d2e2711C0b9887E518FfE1879d5",
                "0xf00A5c61d7411D1Cb8231e9618b7851B4243092c",
                "0x4a9B9e31EFbBade790725CE31F257f0d7D7b2f2c",
                "0xaEE81b0b6b7c409dbB3cA2839f91C172E75Ab781",
                "0xD697eC336A037B047c397464C354982b16fa784B",
                "0x2046CfE33bb5B65A183414FCCB9CB9022dff9dA1",
                "0xF8F353e88F761faa2a5f721A5eCF71Dcd180c93f",
                "0x5c03CC3a68be5e41c7ceBEf47939b6d387E4c5E6",
                "0x95554E6679EA8E8681ceB539C733dEa9F28db119",
                };
        String pubKey = "xpub6BoGYciy75JfrWWm1yJJctrCnEft6RtyECCjHDuhACfmShsSbcSp5ceAZzx1YpMtLfWHCbG6bKqEH9uoUCbvMWAkjSMkMeVuYYf23nWd76X";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Etc.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










