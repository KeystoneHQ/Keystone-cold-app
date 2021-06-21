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

import com.keystone.coinlib.coins.IOST.Iost;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class IostTest {
    @Test
    public void deriveAddress() {
        String pubKey = "xpub6GhXKqoStzyfom3kNWLzMFhLzir4GgPXnzwYMwurCWizsq2yXvZRHJJBny1hzA1YsepvGC9L3dSYPDY3wkuahqDsbK36cHKY4LMtV8DnARm";
        String address = new Iost.Deriver().derive(pubKey);

        assertEquals(address,"4guFd1weqq2Nvfxt6EDSfztUjp44trRzfJMqXumJ3qRt");
    }

}










