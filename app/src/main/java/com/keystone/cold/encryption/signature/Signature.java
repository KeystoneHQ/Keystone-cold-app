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

package com.keystone.cold.encryption.signature;

import androidx.annotation.NonNull;

import com.keystone.cold.encryptioncore.utils.Preconditions;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Migrate from web3j source code
 */
public class Signature {
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    public static int getRecoverIdFromSignature(final BigInteger publicKey, @NonNull BigInteger signR, @NonNull BigInteger signS, @NonNull byte[] message) {
        Preconditions.checkNotNull(publicKey, "public key cannot be null");
        Preconditions.checkArgument(signR.signum() >= 0, "r must be positive");
        Preconditions.checkArgument(signS.signum() >= 0, "s must be positive");
        Preconditions.checkNotNull(message, "message cannot be null");

        int recId = -1;

        for (int i = 0; i < 4; ++i) {
            final BigInteger k = recoverFromSignature(i, signR, signS, message);

            if (k != null && k.equals(publicKey)) {
                recId = i;
                break;
            }
        }

        if (recId == -1) {
            throw new RuntimeException(
                    "Could not construct a recoverable key. This should never happen.");
        }

        return recId;
    }

    public static byte[] decompress(@NonNull final byte[] compressedBytes) {
        Preconditions.checkNotNull(compressedBytes, "key array should not be null");
        Preconditions.checkArgument(compressedBytes.length == 33, String.format("Illegal key bytes length, need 33 but get %s", compressedBytes.length));
        Preconditions.checkArgument(compressedBytes[0] == 2 || compressedBytes[0] == 3, "Illegal compress public key, the first byte of array should be 2 or 3");

        final ECPoint point = CURVE.getCurve().decodePoint(compressedBytes);
        final byte[] unCompressedBytes = point.getEncoded(false);

        return Arrays.copyOfRange(unCompressedBytes, 1, unCompressedBytes.length);
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public
     * key that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     * <p>
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the
     * correct one. Because the key recovery operation yields multiple potential keys, the correct
     * key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs
     * the key you are expecting.</p>
     * <p>
     * <p>If this method returns null it means recovery was not possible and recId should be
     * iterated.</p>
     * <p>
     * <p>Given the above two points, a correct usage of this method is inside a for loop from
     * 0 to 3, and if the output is null OR a key that is not the one you expect, you try again
     * with the next recId.</p>
     *
     * @param recId   Which possible key to recover.
     * @param signR   the R components of the signature.
     * @param signS   the  S components of the signature.
     * @param message Hash of the data that was signed.
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    private static BigInteger recoverFromSignature(final int recId, @NonNull BigInteger signR, @NonNull BigInteger signS, @NonNull byte[] message) {
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        final BigInteger n = CURVE.getN();  // Curve order.
        final BigInteger i = BigInteger.valueOf((long) recId / 2);
        final BigInteger x = signR.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion
        //        routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        //        using the conversion routine specified in Section 2.3.4. If this conversion
        //        routine outputs "invalid", then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.

        final BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }

        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        final ECPoint R = decompressKey(x, (recId & 1) == 1);

        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }

        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        final BigInteger e = new BigInteger(1, message);

        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        final BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        final BigInteger rInv = signR.modInverse(n);
        final BigInteger srInv = rInv.multiply(signS).mod(n);
        final BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        final ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
        final byte[] qBytes = q.getEncoded(false);

        // We remove the prefix
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        final X9IntegerConverter x9 = new X9IntegerConverter();
        final byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));

        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);

        return CURVE.getCurve().decodePoint(compEnc);
    }
}
