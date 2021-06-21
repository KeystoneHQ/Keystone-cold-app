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

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MnemonicTest {

    @Test
    public void generateMnemonic() {
        String entropy = "a6ba4289ed48ecf997634e38c474a15e";
        String mnemonic = MnemonicUtils.generateMnemonic(Hex.decode(entropy));
        assertEquals("pluck spirit pen surround mom lamp frown hawk decide casual choose rose", mnemonic);
    }

    @Test
    public void validationEntropy() {
        String entropy = "a6ba4289ed48ecf997634e38c474a1";
        boolean valid = MnemonicUtils.isValidateEntropy(Hex.decode(entropy));
        assertFalse(valid);

        entropy = "a6ba4289ed48ecf997634e38c474a15e";
        valid = MnemonicUtils.isValidateEntropy(Hex.decode(entropy));
        assertTrue(valid);

        entropy = "";
        valid = MnemonicUtils.isValidateEntropy(Hex.decode(entropy));
        assertFalse(valid);

        entropy = "8f6d4c7384bb4be6aba4747655b79446eedfe0347c4a65b111b0fa921e1b9911";
        valid = MnemonicUtils.isValidateEntropy(Hex.decode(entropy));
        assertTrue(valid);

    }

    @Test
    public void validationMnemonic() {
        String mnemonic = "pluck spirit pen surround mom lamp frown hawk decide casual choose rose";
        boolean valid = MnemonicUtils.validateMnemonic(mnemonic);
        assertTrue(valid);

        mnemonic = "pluck spirit pen surround mom lamp frown hawk decide casual choose";
        valid = MnemonicUtils.validateMnemonic(mnemonic);
        assertFalse(valid);

        mnemonic = "month heavy brother annual regret viable purse model iron pumpkin topic miss unknown useless spin matter grant mass history vivid capable assist gold cream";
        valid = MnemonicUtils.validateMnemonic(mnemonic);
        assertTrue(valid);

        mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
        valid = MnemonicUtils.validateMnemonic(mnemonic);
        assertTrue(valid);

        mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon";
        valid = MnemonicUtils.validateMnemonic(mnemonic);
        assertFalse(valid);
    }
}
