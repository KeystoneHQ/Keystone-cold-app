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

import com.keystone.coinlib.coins.DASH.Dash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class DashTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "XciiKrSHgdFkuL9FTT31qRfTPNUVxX4sPc",
                "XgTmuNhRLoj4F75TqboUgZQVQ37HCgW9Hf",
                "Xpj2zS65KN6r792TQKuAMeJpLMdknrjo1y",
                "Xb9LAffWjcxTCN5GMj5kbZiqN5g7nnkgrv",
                "XyAT2tRaXhdZ4JvvB44Jy9S4qd6JurzaVo",
                "XntqLKJvZbkauFvGLWHsGNMh51K5eiHvHT",
                "XuMqWDM3eabLrze4hnLDtwvzBfQVSNjdqk",
                "Xj3Q7zDq3BYmiPNHrwMgtPNgSKYQyQTs97",
                "XmWeKAdw3DPcFvRFhro8brzhpqPoxvGdny",
                "XfYy4fYEXa6ikpfzsSZS537yjoN3Mhstsv",
                "XyuGNJoxmUiA1unZ7JKiG2g5WS69sDKFBP",
                };
        String pubKey = "xpub6DTnbXgbPo6mrRhgim9sg7Jp571onenuioxgfSDJEREH7wudyDQMDSoTdLQiYq3tbvZVkzcPe7nMgL7mbSixQQcShekfhKt3Wdx6dE8MHCk";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Dash.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










