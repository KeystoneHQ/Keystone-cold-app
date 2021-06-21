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

import com.keystone.coinlib.coins.BTC.Btc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class BtcTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ",
                "352BwoAuZWavjSDgAUUCnbg6a2v3FEzbMn",
                "32Hhx9CM3gYwChY2ZRFtHTYpJCeUaoypkj",
                "3C5VmDeH3x6x9fPm4cft3qVhzvv8R4Ln7K",
                "38c8FFS9W4QEW55WXgo2wX8HZJCutH89VT",
                "3EWeeC2wPyTAsPG7rTRbczbFjvZ8nW41AZ",
                "35yk3RdCHQYptsZAmibMpUt6F9XkK7UVCm",
                "32sNjcbmj4hqthuP6hsQk3BjzVLFLZSLr8",
                "34dKWKb347TJwp1PysTt5faqSs9C1PXrVX",
                "3AzuY1VfwKBgAN3soFnm8BNNAJxx4S3Y7L",
                "3CAgK3r8nDiMcdnqBUL3Yy7JMFDjvkXqHn"
                };
        String pubKey = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Btc.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}
