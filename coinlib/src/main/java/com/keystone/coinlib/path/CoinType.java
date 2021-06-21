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

package com.keystone.coinlib.path;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Represents the 3rd part of a CoinPath path. Create via a {@link Purpose}.
 * m / purpose' / coin_type' / account' / change / address_index
 */
public final class CoinType {
    private final Purpose purpose;
    private final int coinType;
    private final String string;

    CoinType(final Purpose purpose, final int coinType) {
        this.purpose = purpose;
        this.coinType = coinType;
        string = String.format(Locale.US, "%s/%d'", purpose, coinType);
    }

    public int getValue() {
        return coinType;
    }

    public Purpose getParent() {
        return purpose;
    }

    @NonNull
    @Override
    public String toString() {
        return string;
    }

    public Account account(final int account) {
        return new Account(this, account);
    }
}