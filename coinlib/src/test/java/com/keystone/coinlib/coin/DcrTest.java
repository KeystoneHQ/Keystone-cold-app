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

import com.keystone.coinlib.coins.DCR.Dcr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class DcrTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "DsYvV7Q6ymU2WFzBFdVzNyUk7UXAE2dVyYd",
                "DsbTLcGY9NHNqVvQZrJyXLcLN76SVrzBodq",
                "DsTznosWuFjz94pjTKoAaEWd8LUVdCYfBfc",
                "DsWzdC3CtWPy5ZqY7pTQH35Gm5JhH9mMe2c",
                "DsbWz3eYSPRJk8DnehixbaDAXcVhUdz4p9u",
                "DsmpowTXxpUxYwtwHgSHoixsCBcJ4gZZCZ7",
                "Dsf2hAR1ZainZdRFY4fWi1ZM6vPJevsuXfn",
                "DsSHLUANtfo82zVyLiTgi6SWQZdgSXAYrAc",
                "Dsa76GyVEETpToSvWATPUtiXSpPbNmQHwg1",
                "DsiC8hxMC2ApnouYUwMK1L4c3wbHezq5Gwb",
                "DsgKnFzMq6kz4JWYWfaywo3LNhMBhJZZsDS",
        };
        String pubKey = "xpub6BmQYH48mM1hm8B2TDXg7oDfa7dZfHVgi7pWFMsmSuJ3rVwi2YEgjZMN1cBFQtieHPgmoY3YQx5G85XyxMvppPxJToxmWjo64gaNutTn6iw";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Dcr.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










