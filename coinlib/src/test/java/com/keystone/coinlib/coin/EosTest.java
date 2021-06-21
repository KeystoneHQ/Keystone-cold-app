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

import com.keystone.coinlib.coins.EOS.Eos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class EosTest {
    @Test
    public void deriveAddress() {
        String pubKey = "xpub6BffWuUvhRpycrEq9H2gQDNT1xtzu2pfNvaf2j6JMxErTFbwAX7yaL5hCocYJwaKxmYgpNuoXkGFKHQv2brE1XEmX1aneH3AHtUrR8tMV6H";
        String address = new Eos.Deriver().derive(pubKey,0,0);

        assertEquals(address,"EOS86B1XBB2B7u3QcdM5tuuTdRMLFfVDzsTcjHvDqcFq78MSstUxp");
    }

}










