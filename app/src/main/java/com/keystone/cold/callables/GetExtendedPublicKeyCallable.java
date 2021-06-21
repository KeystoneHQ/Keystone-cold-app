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

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

import static com.keystone.cold.db.PresetData.getCurveByPath;

public class GetExtendedPublicKeyCallable implements Callable<String> {
    private final String pubKeyPath;
    private final Coins.CURVE curve;
    private final boolean isMainWallet;


    public GetExtendedPublicKeyCallable(String pubKeyPath) {
        this.curve = getCurveByPath(pubKeyPath);
        this.pubKeyPath = curve == Coins.CURVE.SR25519 ? pubKeyPath : pubKeyPath.toUpperCase();
        isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
    }

    @Override
    public String call() {
        final Callable<Packet> callable = new BlockingCallable(
                new Packet.Builder(CONSTANTS.METHODS.GET_EXTENDED_PUBLICKEY)
                        .addBytePayload(CONSTANTS.TAGS.CURVE, getCurveTag())
                        .addBytePayload(CONSTANTS.TAGS.WALLET_FLAG, isMainWallet? 0 : 0x50)
                        .addTextPayload(CONSTANTS.TAGS.PATH, pubKeyPath).build());

        final Packet result;
        try {
            result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.EXTEND_PUB_KEY);
            if (payload != null) {
                return payload.toUtf8();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
