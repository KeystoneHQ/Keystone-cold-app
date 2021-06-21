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

import java.util.*;

public class TransactionFlagMap {
    private final static List<Flags> flags = new ArrayList<>();
    private final static List<Flags> accountSetFlags = new ArrayList<>();
    static {
        // Constraints flags:
        flags.add(new Flags(TransactionFlag.FullyCanonicalSig, "FullyCanonicalSig","Constraints"));
        flags.add(new Flags(TransactionFlag.UniversalMask, "UniversalMask", "Constraints"));
        // AccountSet flags:
        flags.add(new Flags(TransactionFlag.RequireDestTag, "RequireDestTag", "AccountSet"));
        flags.add(new Flags(TransactionFlag.OptionalDestTag, "OptionalDestTag", "AccountSet"));
        flags.add(new Flags(TransactionFlag.RequireAuth, "RequireAuth", "AccountSet"));
        flags.add(new Flags(TransactionFlag.OptionalAuth, "OptionalAuth", "AccountSet"));
        flags.add(new Flags(TransactionFlag.DisallowXRP, "DisallowXRP", "AccountSet"));
        flags.add(new Flags(TransactionFlag.AllowXRP, "AllowXRP", "AccountSet"));
        flags.add(new Flags(TransactionFlag.AccountSetMask, "AccountSetMask", "AccountSet"));
        // OfferCreate flags:
        flags.add(new Flags(TransactionFlag.Passive, "Passive", "OfferCreate"));
        flags.add(new Flags(TransactionFlag.ImmediateOrCancel, "ImmediateOrCancel", "OfferCreate"));
        flags.add(new Flags(TransactionFlag.FillOrKill, "FillOrKill", "OfferCreate"));
        flags.add(new Flags(TransactionFlag.Sell, "Sell", "OfferCreate"));
        flags.add(new Flags(TransactionFlag.OfferCreateMask, "OfferCreateMask", "OfferCreate"));
        // Payment flags:
        flags.add(new Flags(TransactionFlag.NoRippleDirect, "NoRippleDirect", "Payment"));
        flags.add(new Flags(TransactionFlag.PartialPayment, "PartialPayment", "Payment"));
        flags.add(new Flags(TransactionFlag.LimitQuality, "LimitQuality", "Payment"));
        flags.add(new Flags(TransactionFlag.PaymentMask, "PaymentMask", "Payment"));
        // PaymentChannelClaim flags:
        flags.add(new Flags(TransactionFlag.Renew, "Renew", "PaymentChannelClaim"));
        flags.add(new Flags(TransactionFlag.Close, "Close", "PaymentChannelClaim"));
        flags.add(new Flags(TransactionFlag.PaymentChannelClaimMask, "PaymentChannelClaimMask", "PaymentChannelClaim"));
        // TrustSet flags:
        flags.add(new Flags(TransactionFlag.SetAuth, "SetAuth", "TrustSet"));
        flags.add(new Flags(TransactionFlag.SetNoRipple, "SetNoRipple", "TrustSet"));
        flags.add(new Flags(TransactionFlag.ClearNoRipple, "ClearNoRipple", "TrustSet"));
        flags.add(new Flags(TransactionFlag.SetFreeze, "SetFreeze", "TrustSet"));
        flags.add(new Flags(TransactionFlag.ClearFreeze, "ClearFreeze", "TrustSet"));
        flags.add(new Flags(TransactionFlag.TrustSetMask, "TrustSetMask", "TrustSet"));
        // AccountSet SetFlag/ClearFlag values
        accountSetFlags.add(new Flags(TransactionFlag.asfRequireDest, "asfRequireDest", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfRequireAuth, "asfRequireAuth", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfDisallowXRP, "asfDisallowXRP", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfDisableMaster, "asfDisableMaster", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfAccountTxnID, "asfAccountTxnID", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfNoFreeze, "asfNoFreeze", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfGlobalFreeze, "asfGlobalFreeze", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfDefaultRipple, "asfDefaultRipple", "AccountSetFlag"));
        accountSetFlags.add(new Flags(TransactionFlag.asfDepositAuth, "asfDepositAuth", "AccountSetFlag"));
    }

    public static String getString(long flag, String transactionType) {
       return  flags.stream().filter(f -> f.transactionType.equals(transactionType))
                .filter(f -> TransactionFlag.hasFlag(flag, f.flag))
                .map(f -> f.flagName)
                .reduce((s1,s2) -> s1 + ", " + s2).orElse(null);
    }

    public static String getAccountSetFlagsString(long flag, String transactionType) {
        return  accountSetFlags.stream().filter(f -> f.transactionType.equals(transactionType))
                .filter(f -> f.flag == flag)
                .findFirst()
                .map(f -> f.flagName).orElse(null);
    }

    static class Flags {
        long flag;
        String flagName;
        String transactionType;

        public Flags(long flag, String flagName, String transactionType) {
            this.flag = flag;
            this.flagName = flagName;
            this.transactionType = transactionType;
        }
    }
}
