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

package com.keystone.cold.encryption.interception;

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryption.signature.Signature;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

class Secp256k1SignIntercept implements InterceptManager {

    private static boolean isCanonical(byte[] sigs) {
        return (sigs[0] & 0x80) == 0
                && !(sigs[0] == 0 && ((sigs[1] & 0x80) == 0))
                && (sigs[32] & 0x80) == 0
                && !(sigs[32] == 0 && ((sigs[33] & 0x80) == 0));
    }

    @Override
    public int getId() {
        return CONSTANTS.METHODS.SIGN;
    }

    @NonNull
    @Override
    public Intercept getIntercept() {
        return new InterceptImpl();
    }

    private static final class InterceptImpl implements Intercept {
        private String txHash;

        @Override
        public void preIntercept(@NonNull Packet packet) {
            txHash = Objects.requireNonNull(
                    Objects.requireNonNull(packet.getPayload(CONSTANTS.TAGS.TX_HASH))
                            .toHex());
        }

        @Override
        public void postIntercept(@NonNull Packet packet) {
            final String publicKey = Objects.requireNonNull(packet.getPayload(CONSTANTS.TAGS.PUBLIC_KEY_HASH)).toHex();
            final String signed = Objects.requireNonNull(packet.getPayload(CONSTANTS.TAGS.SIGNED)).toHex();

            final byte[] signBytes = ByteFormatter.hex2bytes(signed);
            if (!isCanonical(signBytes)) {
                throw new RuntimeException("couldn't find a canonical signature");
            }

            final byte[] signR = Arrays.copyOf(signBytes, 32);
            final byte[] signS = Arrays.copyOfRange(signBytes, 32, 64);

            final BigInteger publicKeyInt = new BigInteger(publicKey, 16);
            final int recId = Signature.getRecoverIdFromSignature(publicKeyInt, new BigInteger(1, signR), new BigInteger(1, signS), Hex.decode(txHash));
            final byte[] signBytesWithRecId = new byte[signBytes.length + 1];

            System.arraycopy(signBytes, 0, signBytesWithRecId, 0, signBytes.length);
            signBytesWithRecId[signBytesWithRecId.length - 1] = (byte) recId;

            packet.getPayloads().put(CONSTANTS.TAGS.SIGNED, new Payload(signBytesWithRecId));
        }

    }

}
