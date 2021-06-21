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

import android.util.Log;

import com.keystone.coinlib.AES;
import com.keystone.coinlib.Der;
import com.keystone.coinlib.RSA;
import com.keystone.coinlib.Secp256r1;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class WebAuthCallableUpgrade implements Callable<String> {

    private byte[] encrypted;
    private byte[] signature;

    public WebAuthCallableUpgrade(byte[] bytes, String rsaPrivKey, byte[] r1pubkey) {
        Log.w("WebAuth", " bytes :" + Hex.toHexString(bytes));
        try {
            byte[] encryptAesKeyIv = Arrays.copyOfRange(bytes, 0,512);
            byte[] encryptData = Arrays.copyOfRange(bytes, 512,bytes.length - 64);
            byte[] r1SigRS = Arrays.copyOfRange(bytes, bytes.length - 64, bytes.length);

            byte[] r1SigDER  = Der.toDer(r1SigRS);

            boolean verified = Secp256r1.verify(r1pubkey, Arrays.copyOfRange(bytes, 0,bytes.length - 64), r1SigDER);
            if (verified) {
                byte[] keyAndIv = RSA.decrypt(encryptAesKeyIv, rsaPrivKey);
                byte[] aesIv = Arrays.copyOfRange(keyAndIv, 0,16);
                byte[] aesKey = Arrays.copyOfRange(keyAndIv, 16,32);

                byte[] dataForChip = AES.decrypt(encryptData, aesKey, aesIv);
                Log.w("WebAuth", " dataForChip :" + Hex.toHexString(dataForChip));
                this.encrypted = Arrays.copyOfRange(dataForChip, 0, dataForChip.length - 64);
                this.signature = Arrays.copyOfRange(dataForChip, dataForChip.length - 64, dataForChip.length);
            } else {
                Log.w("WebAuth","verified fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.encrypted = null;
            this.signature = null;
        }

    }

    @Override
    public String call() {
        if (encrypted == null || signature == null) {
            return null;
        }
        final Callable<Packet> callable = new BlockingCallable(
                new Packet.Builder(CONSTANTS.METHODS.WEB_AUTH)
                        .addHexPayload(CONSTANTS.TAGS.ENCRYPTED, Hex.toHexString(encrypted))
                        .addHexPayload(CONSTANTS.TAGS.SIGNATURE, Hex.toHexString(signature)).build());
        final Packet result;
        try {
            result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.DECRYPTED);
            if (payload != null) {
                return payload.toUtf8();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
