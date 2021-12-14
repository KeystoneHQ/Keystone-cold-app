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

package com.keystone.coinlib.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Signer {

    private final String publicKey;

    //for utxo coins publicKey is needed
    public Signer(@Nullable String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public abstract String sign(String hex);

    public static boolean shouldProvidePublicKey(@NonNull String coinCode) {
        switch (coinCode) {
            case "BTC":
            case "BTC_LEGACY":
            case "BTC_NATIVE_SEGWIT":
            case "BTC_TESTNET_SEGWIT":
            case "BTC_TESTNET_LEGACY":
            case "BTC_TESTNET_NATIVE_SEGWIT":
            case "XTN":
            case "XRP":
            case "BCH":
            case "LTC":
            case "DASH":
            case "IOST":
            case "FIRO":
            case "DCR":
            case "DOT":
            case "KSM":
                return true;
            default:
                return false;
        }

    }
}
