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

package com.keystone.cold.encryptioncore.cipher;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DESTest {

    @Test
    public void encrypt() throws Exception {
        // 24 69 B5 84 92 60 F3 14 7F C6 C7 8C 30 5B E8 AE BB 66 E8 FE E7 5B E0 39
        // 01 23 45 67 89 AD CD EF 01 23 45 67 89 AD CD EF 01 23 45 67 89 AD CD EF
        // FE DC BA 98 76 54 32 10
        // 00 01 00 02 08 01 08 01 00 08 01 23 45 67 89 AB CD EF
        assertArrayEquals(new byte[]{
                0x24, 0x69, (byte) 0xB5, (byte) 0x84, (byte) 0x92, 0x60, (byte) 0xF3, 0x14,
                0x7F, (byte) 0xC6, (byte) 0xC7, (byte) 0x8C, 0x30, 0x5B, (byte) 0xE8, (byte) 0xAE,
                (byte) 0xBB, 0x66, (byte) 0xE8, (byte) 0xFE, (byte) 0xE7, 0x5B, (byte) 0xE0, 0x39
        }, DES.encrypt(new byte[]{
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
        }, new byte[]{
                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98, 0x76, 0x54, 0x32, 0x10
        }, new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x08, 0x01, 0x08, 0x01, 0x00, 0x08, 0x01, 0x23, 0x45,
                0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
        }));
    }

    @Test
    public void decrypt() throws Exception {
        assertArrayEquals(new byte[]{
                0x00, 0x01, 0x00, 0x02, 0x08, 0x01, 0x08, 0x01, 0x00, 0x08, 0x01, 0x23, 0x45,
                0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
        }, DES.decrypt(new byte[]{
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAD, (byte) 0xCD, (byte) 0xEF,
        }, new byte[]{
                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98, 0x76, 0x54, 0x32, 0x10
        }, new byte[]{
                0x24, 0x69, (byte) 0xB5, (byte) 0x84, (byte) 0x92, 0x60, (byte) 0xF3, 0x14,
                0x7F, (byte) 0xC6, (byte) 0xC7, (byte) 0x8C, 0x30, 0x5B, (byte) 0xE8, (byte) 0xAE,
                (byte) 0xBB, 0x66, (byte) 0xE8, (byte) 0xFE, (byte) 0xE7, 0x5B, (byte) 0xE0, 0x39
        }));
    }
}