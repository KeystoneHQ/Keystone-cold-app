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

import com.keystone.coinlib.coins.FIRO.Firo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class FiroTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "aJtu4Ztv64yCUZYm9YzQ7b3Hyrii2foaj8",
                "aJJvE5kRvBJXJWeBcxScAf2ABr4vZVCotp",
                "aGQC2rasPV2tiJnFJTqbevAniozqZDPpcA",
                "a6pzbbDbqgCbKupySGuVWX2QewCGcSNFac",
                "a3fY53SF4CdKeWAwMcUMQMjf7Y9b9tcxqS",
                "aAFzem9L6FTT6peetFMoinJVeaCE2YZWDR",
                "aNZUbMNuKEKxNSKvXCdNHt8kUTZoRQ8PM2",
                "a6sFxJEsvwFvKCdaxhjGf1LSWYHTAfBwEE",
                "aGcTa2jiSkyFLVGsLjtJZG5kCYxtWbbUhB",
                "a2gcBybNBzDqsbAU5S78ug8GUKrnm1STwS",
                "aBCFtxBN915aLPYbCADQULzUv4qYiXNW3X",
                };
        String pubKey = "xpub6CKMZBhyfgYMeK7UuVCwF56BXsm1kVbUQwmB7bDU2i2cGx2VXNwR7Z5tnGsuZbjc35LQvhyZJasrX12adMdAqtFno9fLJJxSksPDU7W3SEk";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Firo.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










