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

import com.keystone.coinlib.utils.Arith;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArithTest {

    @Test
    public void add() {
        assertEquals(0.06, Arith.add(0.01, 0.05), 0);
    }

    @Test
    public void sub() {
        assertEquals(0.04, Arith.sub(0.05, 0.01), 0);
    }

    @Test
    public void mul() {
        assertEquals(0.0005, Arith.mul(0.05, 0.01), 0);
    }

    @Test
    public void div() {
        assertEquals(5, Arith.div(0.05, 0.01), 0);
    }
}
