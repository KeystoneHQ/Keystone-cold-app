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

public class TransactionFlag {
    public static long
            FullyCanonicalSig = 0x80000000L,
            Universal = FullyCanonicalSig,
            UniversalMask = ~Universal,

    // AccountSet flags:
    RequireDestTag = 0x00010000,
            OptionalDestTag = 0x00020000,
            RequireAuth = 0x00040000,
            OptionalAuth = 0x00080000,
            DisallowXRP = 0x00100000,
            AllowXRP = 0x00200000,
            AccountSetMask = ~(Universal | RequireDestTag | OptionalDestTag
                    | RequireAuth | OptionalAuth
                    | DisallowXRP | AllowXRP),

    // AccountSet SetFlag/ClearFlag values
    asfRequireDest = 1,
            asfRequireAuth = 2,
            asfDisallowXRP = 3,
            asfDisableMaster = 4,
            asfAccountTxnID = 5,
            asfNoFreeze = 6,
            asfGlobalFreeze = 7,
            asfDefaultRipple = 8,
            asfDepositAuth = 9,

    // OfferCreate flags:
    Passive = 0x00010000,
            ImmediateOrCancel = 0x00020000,
            FillOrKill = 0x00040000,
            Sell = 0x00080000,
            OfferCreateMask = ~(Universal | Passive | ImmediateOrCancel | FillOrKill | Sell),

    // Payment flags:
    NoRippleDirect = 0x00010000,
            PartialPayment = 0x00020000,
            LimitQuality = 0x00040000,
            PaymentMask = ~(Universal | PartialPayment | LimitQuality | NoRippleDirect),

    // PaymentChannelClaim flags:
    Renew = 0x00010000,
            Close = 0x00020000,
            PaymentChannelClaimMask = ~(Renew | Close),

    // TrustSet flags:
    SetAuth = 0x00010000,
            SetNoRipple = 0x00020000,
            ClearNoRipple = 0x00040000,
            SetFreeze = 0x00100000,
            ClearFreeze = 0x00200000,
            TrustSetMask = ~(Universal | SetAuth | SetNoRipple | ClearNoRipple | SetFreeze | ClearFreeze);

    public static boolean hasFlag(long flags, long flag) {
        return (flags & flag) == flag;
    }
}