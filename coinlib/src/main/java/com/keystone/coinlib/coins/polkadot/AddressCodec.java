/*
 *
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
 *
 */

package com.keystone.coinlib.coins.polkadot;

import org.bitcoinj.core.Base58;
import org.bouncycastle.crypto.digests.Blake2bDigest;

import java.util.Arrays;

import static com.keystone.coinlib.Util.concat;

public class AddressCodec {
    private final static byte[] SS58_PREFIX = "SS58PRE".getBytes();

    public static String encodeAddress(byte[] key) {
        return encodeAddress(key, (byte) 42);
    }

    public static String encodeAddress(byte[] key, byte prefix) {
        byte[] input = concat(new byte[]{prefix}, key);
        byte[] hash = sshash(input);
        byte[] bytes = concat(input, Arrays.copyOfRange(hash, 0, 2));
        return Base58.encode(bytes);
    }

    public static byte[] sshash(byte[] key) {
        return blake2b(concat(SS58_PREFIX, key), 512);
    }

    public static byte[] blake2b(byte[] data, int bitLength) {
        int byteLength = (int) Math.ceil(bitLength / 8F);
        Blake2bDigest digest = new Blake2bDigest(null, byteLength, null, null);
        digest.reset();
        digest.update(data, 0, data.length);
        byte[] keyedHash = new byte[64];
        int digestLength = digest.doFinal(keyedHash, 0);
        return Arrays.copyOfRange(keyedHash, 0, digestLength);
    }

    public static byte[] decodeAddress(String address) throws Exception {
        byte[] decoded = Base58.decode(address);
        int publicKeyLength = checkChecksum(decoded);
        if(publicKeyLength == -1) {
            throw new Exception("invalid address");
        } else {
            byte[] publicKey = new byte[publicKeyLength -1];
            System.arraycopy(decoded, 1, publicKey, 0, publicKeyLength-1);
            return publicKey;
        }
    }

    private static int checkChecksum(byte[] decoded) {
        boolean isPublicKey = decoded.length == 35 || decoded.length == 36;
        int length = decoded.length - (isPublicKey ? 2 : 1);
        byte[] sub = new byte[length];
        System.arraycopy(decoded, 0, sub, 0, length);
        byte[] hash = sshash(sub);
        boolean isValid = isPublicKey
                ? decoded[decoded.length - 2] == hash[0] && decoded[decoded.length - 1] == hash[1]
                : decoded[decoded.length - 1] == hash[0];

        if (isValid) {
            return length;
        } else {
            return -1;
        }
    }
}