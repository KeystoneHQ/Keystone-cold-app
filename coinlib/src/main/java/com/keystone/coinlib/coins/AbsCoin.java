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

package com.keystone.coinlib.coins;

import androidx.annotation.NonNull;

import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.v8.CoinImpl;

import java.lang.reflect.InvocationTargetException;

public abstract class AbsCoin implements Coin {
    protected final Coin impl;

    protected AbsCoin(Coin impl) {
        this.impl = impl;
    }

    public static AbsCoin newInstance(String coinCode) {
        Coin impl = new CoinImpl(coinCode);
        try {
            Class<?> clazz = Class.forName(CoinReflect.getCoinClassByCoinCode(coinCode));
            return (AbsCoin) clazz.getDeclaredConstructor(Coin.class).newInstance(impl);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        impl.generateTransaction(tx, callback, signers);
    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return impl.signMessage(message, signer);
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return impl.generateAddress(publicKey);
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return impl.isAddressValid(address);
    }
}
