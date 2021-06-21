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

import com.keystone.coinlib.utils.Bip39;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void validateMnemonic() {
        String mnemonic = "army van defense carry jealous true garbage claim echo media make crunch";
        assertTrue(Bip39.validateMnemonic(mnemonic));
    }

    @Test
    public void testDecode() {
        String s = "H4sIAAAAAAAAA42ST3PaMBDFv0rGl1wygyXrD+YWHEKmxB0COAE6PUhaOVYAi1gyjZ3Jd69oO50eeshpR2/fzr7frN6jk26csXU0QlcRaKcac/S/3pGy0l6cxL71F6+NsqAvjo31Vtl9FKzCi2j0HvnuqIPXdbUKagUru9PnYcKI0hy0QqmMKeUpK6lgQ8aSGAjnMmYakJZIQOgBTjgnCHMkKMepxgQBksB1XDKUnA1KSsDpMDQoRULLBKVMYVbGQ1omCaA0TFIQWJesVIxCrLmSmmutMOJUAGCMNKdDVYo0CXtZCKusqV00+vYeGQiJQbgqqM4L3wY5MrVQ3px00IRStq39b3MFc+GrYMgHhFwO6OUgvgyet2Mrg3guLNtMXlzB+nmeuOubJW5li8VWT9+yQ5LRmZC77GhJs/sq19PruzTnzaL4QiY0m5muADgs+maZ2eO8N241bta7MrshlnK+nuXpoe+2fo7WIE3f7c7hABrt3L2un8+xwhmNy9u9N848R6NS7J3++P5x9YeyP1P/i/kpSJSw/2Fm3awupmPy+JLfbzdTsTyNzUtxX6E7wIU5dOlk1tGuePDpRr9J7jB50jhb5ZRUT+Jhu6myfrlg3eT22lH1MFz9kGJZ02TJ7hbNQdzu4PlV9XJIT5/GDKDhrnt4/PurY4zZx083/fTZ5wIAAA==";
        byte[] ss = Base64.decode(s);
        System.out.println(new String(ss));
        //ss = unGZip(ss);

        // System.out.println(new String(ss));

        String hash = "e787ce47d3c530ef04266d739ade4ef262b6ef9f";
        System.out.println(Long.parseLong("e787ce", 16));

    }



    @Test
    public void testHexToInt() {
        assertEquals(Integer.parseInt("ff", 16), 255);
    }

}