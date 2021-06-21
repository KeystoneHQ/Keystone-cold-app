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

package com.keystone.cold.encryption;

import androidx.annotation.Nullable;

import com.keystone.coinlib.interfaces.Signer;
import com.keystone.cold.callables.SignTxCallable;

import java.util.Objects;

public class ChipSigner extends Signer {

    private final String privKeyPath;
    private final String authToken;

    public ChipSigner(String path, String authToken) {
        this(path, authToken, null);
    }

    public ChipSigner(String path, String authToken, @Nullable String publicKey) {
        super(publicKey);
        this.privKeyPath = Objects.requireNonNull(path);
        this.authToken = authToken;
    }

    @Override
    public String sign(String data) {
        SignTxCallable callable = new SignTxCallable(privKeyPath, data, authToken);
        return callable.call();
    }
}
