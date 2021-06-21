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

import com.keystone.coinlib.coins.BCH.Bch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class BchTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "1HXQeKa8YSg1bEoToLqxXcHfm4Wrt4qk3V",
                "18ZyEqa4ioSiTztN5zHRNoQCr5ZYtMNTjJ",
                "18xv4x9EJzCQ8JGQWdBELL8tJUD7BHNYkf",
                "1CGn89GG6o6aLSotHc1YZWoJ6jnKaCiWcH",
                "16ruo7A9NGzqwpdatMMKaE1yWA6cfKXmrU",
                "1C5QXNRmNVdEyh91nSrKp66TS1tkaFn5Jy",
                "1Nk2jDqbMbxQNUcgRw17eiByUBw7s1VbC2",
                "1NYx23tK96Utp75rXZGbY3YJfAGqjBdVhn",
                "1EVdGw9JLbSFLaRHrWrQp7ULWM4px51725",
                "19emQ5tpdoYDKd3aia7wo9PQtwej88bpsz",
                "1JBKtGPGXPFb3pDeK1cF8czdgvYMzaxyE5"
                };

        String[] cashAddress = new String[] {
                "qz65ywjm92m27wshfnew2w3us5vsgxqkxc55t9lqcw",
                "qpfs2gcfwg322segkj4h30du4vtjyvsxtq6msk90a5",
                "qpt4cr4juduwl36w35rwfwvz6am2z7mxcg8a84k28n",
                "qpa62j36dy2xjjlzcaavm9p8fe5um6p2ru9zysy4u4",
                "qpqyj9gqwyy0ghac582cec90h7jjls5zev5ug44pqx",
                "qpuhatcelvquhr37gatm5dd9ssll3adkzuch8ugqu6",
                "qrh8m7rp5ggkwyj4dc553480pyhr06kzq599ddue4c",
                "qrkx2a5t6z89fmpn4ppmjvnp2mlleds9tvnscqluqf",
                "qz2q8yqwm4693ukfjrh44l39cwp7p9dwussj65sdn8",
                "qp0w2a2ukdpwezjcwjs7nr4twjdwwz58x596n9ysw0",
                "qz7xurac6mvkcptrjcxaalt3mra7rqnscyg7mjm9e6"
        };
        String pubKey = "xpub6CjD9XYc1hEKcAMsSasAA87Mw8bSUr6WQKrJ1ErLofJPP9sxeZ3sh1dH2S5ywQTRNrXsfXzT686jJNdX2m9KhvMDh4eQM9AdSkkQLLMbDG6";
        for (int i = 0 ; i < cashAddress.length; i++) {
            String address = new Bch.Deriver().derive(pubKey,0,i);
            assertEquals(address,cashAddress[i]);
        }
        for (int i = 0 ; i < addr.length; i++) {
            assertEquals(Bch.toCashAddress(addr[i]),cashAddress[i]);
        }

    }

}
