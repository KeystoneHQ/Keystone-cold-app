/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.XRP;

import androidx.annotation.Nullable;

import com.keystone.coinlib.coins.XRP.transcationtype.AccountDelete;
import com.keystone.coinlib.coins.XRP.transcationtype.AccountSet;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCash;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.DepositPreauth;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowFinish;
import com.keystone.coinlib.coins.XRP.transcationtype.OfferCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.OfferCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.Payment;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelClaim;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelFund;
import com.keystone.coinlib.coins.XRP.transcationtype.SetRegularKey;
import com.keystone.coinlib.coins.XRP.transcationtype.SignerListSet;
import com.keystone.coinlib.coins.XRP.transcationtype.TrustSet;


import java.util.HashMap;
import java.util.Map;

public class SupportTransactions {
    private static final Map<String,XrpTransaction> sMap;

    static {
        sMap = new HashMap<>();
        register(new AccountSet());
        register(new AccountDelete());
        register(new CheckCancel());
        register(new CheckCash());
        register(new CheckCreate());
        register(new DepositPreauth());
        register(new EscrowCancel());
        register(new EscrowCreate());
        register(new EscrowFinish());
        register(new OfferCancel());
        register(new OfferCreate());
        register(new Payment());
        register(new PaymentChannelClaim());
        register(new PaymentChannelCreate());
        register(new PaymentChannelFund());
        register(new SetRegularKey());
        register(new SignerListSet());
        register(new TrustSet());
    }

    @Nullable
    public static XrpTransaction get(String type) {
        return sMap.get(type);
    }

    private static void register(XrpTransaction xrpTransaction) {
        sMap.put(xrpTransaction.getTransactionType(), xrpTransaction);
    }
}
