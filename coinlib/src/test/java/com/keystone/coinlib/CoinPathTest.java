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

package com.keystone.coinlib;

import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.path.Account;
import com.keystone.coinlib.path.AddressIndex;
import com.keystone.coinlib.path.Change;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.path.CoinType;
import com.keystone.coinlib.path.Purpose;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoinPathTest {
    @Test(expected = InvalidPathException.class)
    public void parseParse() throws InvalidPathException {
        String path = "M/49'/0'/0'/0";
        CoinPath.parsePath(path);
    }

    @Test
    public void parseParse1() throws InvalidPathException {
        String path = "M/49'/0'/0'/0/0";
        CoinPath.parsePath(path);
        path = "M/49'/0'/0'/0/111";
        CoinPath.parsePath(path);
        path = "M/49'/0'/0'/0/111";
        CoinPath.parsePath(path);
        path = "M/49'/0'/0'/1/111";
        CoinPath.parsePath(path);
    }

    @Test
    public void parseParse2() throws InvalidPathException {
        String path = "M/49'/0'/0'/0'/0'";
        CoinPath.parsePath(path, true);
    }

    @Test(expected = InvalidPathException.class)
    public void parseParse3() throws InvalidPathException {
        String path = "M/49'/0'/0'/0/0";
        CoinPath.parsePath(path, true);
    }

    @Test(expected = InvalidPathException.class)
    public void parseParse4() throws InvalidPathException {
        String path = "M/49'/0'/0'/0'/0'";
        CoinPath.parsePath(path, false);
    }

    @Test
    public void parseParse5() throws InvalidPathException {
        String path = "M/49'/0'/0'/0'/121212'";
        CoinPath.parsePath(path, true);
    }

    @Test
    public void parseParse6() throws InvalidPathException {
        String path = "M/49'/22'/12'/0/123";
        AddressIndex addressIndex = CoinPath.parsePath(path);
        assertEquals(123, addressIndex.getValue());

        Change change = addressIndex.getParent();
        assertEquals(0, change.getValue());

        Account account = change.getParent();
        assertEquals(12, account.getValue());

        CoinType coin = account.getParent();
        assertEquals(22, coin.getValue());

        Purpose purpose = coin.getParent();
        assertEquals(49, purpose.getValue());

    }


}
