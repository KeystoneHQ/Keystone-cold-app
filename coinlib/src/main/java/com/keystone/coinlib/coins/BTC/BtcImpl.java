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

package com.keystone.coinlib.coins.BTC;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.v8.CoinImpl;
import com.eclipsesource.v8.V8Object;

public class BtcImpl extends CoinImpl {
    public BtcImpl() {
        super("BTC");
    }

    SignTxResult generateOmniTx(@NonNull AbsTx tx, Signer... signers) {
        V8Object txData = constructTxData(tx.getMetaData());
        return signTxImpl(txData, "generateOmniTransactionSync", signers);
    }
}
