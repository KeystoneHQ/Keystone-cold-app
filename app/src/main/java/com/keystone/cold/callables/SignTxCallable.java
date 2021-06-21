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

package com.keystone.cold.callables;

import androidx.annotation.NonNull;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryption.signature.Signature;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

public class SignTxCallable implements Callable<String> {

    private final String hdPath;
    private final String hash;
    private final Coins.CURVE curve;
    private final String authToken;
    private final boolean isMainWallet;

    public SignTxCallable(String path, String hash, String authToken) {
        this.hdPath = path;
        this.hash = hash;
        this.curve = getCurveByPath(path);
        this.authToken = authToken;
        isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
    }

    private static boolean isCanonical(byte[] sigs) {
        return (sigs[0] & 0x80) == 0
                && !(sigs[0] == 0 && ((sigs[1] & 0x80) == 0))
                && (sigs[32] & 0x80) == 0
                && !(sigs[32] == 0 && ((sigs[33] & 0x80) == 0));
    }

    private void postIntercept(@NonNull Packet packet) {
        final String publicKey = Objects.requireNonNull(packet.getPayload(CONSTANTS.TAGS.PUBLIC_KEY_HASH)).toHex();
        final String signed = Objects.requireNonNull(packet.getPayload(CONSTANTS.TAGS.SIGNED)).toHex();

        final byte[] signBytes = ByteFormatter.hex2bytes(signed);
        if (!isCanonical(signBytes)) {
            throw new RuntimeException("couldn't find a canonical signature");
        }

        final byte[] signR = Arrays.copyOf(signBytes, 32);
        final byte[] signS = Arrays.copyOfRange(signBytes, 32, 64);

        final BigInteger publicKeyInt = new BigInteger(publicKey, 16);
        final int recId = Signature.getRecoverIdFromSignature(publicKeyInt,
                new BigInteger(1, signR),
                new BigInteger(1, signS), Hex.decode(hash));
        final byte[] signBytesWithRecId = new byte[signBytes.length + 1];

        System.arraycopy(signBytes, 0, signBytesWithRecId, 0, signBytes.length);
        signBytesWithRecId[signBytesWithRecId.length - 1] = (byte) recId;

        packet.getPayloads().put(CONSTANTS.TAGS.SIGNED, new Payload(signBytesWithRecId));
    }

    @Override
    public String call() {
        try {
            final Callable<Packet> callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.SIGN)
                            .addTextPayload(CONSTANTS.TAGS.PATH, hdPath)
                            .addBytePayload(CONSTANTS.TAGS.WALLET_FLAG, isMainWallet? 0 : 0x50)
                            .addBytePayload(CONSTANTS.TAGS.CURVE, getCurveTag())
                            .addHexPayload(CONSTANTS.TAGS.AUTH_TOKEN, authToken)
                            .addHexPayload(CONSTANTS.TAGS.TX_HASH, hash).build());
            final Packet result = callable.call();

            // secp256k1 should calculate recovery id
            if (curve == Coins.CURVE.SECP256K1) {
                postIntercept(result);
            }

            final Payload payload = result.getPayload(CONSTANTS.TAGS.SIGNED);
            if (payload != null) {
                return payload.toHex();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Coins.CURVE getCurveByPath(String pubKeyPath) {
        try {
            String[] strs = pubKeyPath.split("/");
            int coinIndex;
            if (strs[2].endsWith("'")) {
                coinIndex = Integer.parseInt(strs[2].substring(0, strs[2].length() - 1));
            } else {
                coinIndex = Integer.parseInt(strs[2]);
            }
            return Coins.curveFromCoinCode(Coins.coinCodeOfIndex(coinIndex));
        }catch (Exception e) {
            return Coins.CURVE.SR25519;
        }
    }

    private int getCurveTag() {
        int value = 0;
        switch (curve) {
            case SECP256K1:
                break;
            case SECP256R1:
                value = 1;
                break;
            case ED25519:
                value = 2;
                break;
            case SR25519:
                value = 3;
                break;
        }

        return value;
    }
}
