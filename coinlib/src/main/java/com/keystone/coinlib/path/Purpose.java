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

public final class Purpose {
    private final int purpose;
    private final String toString;

    Purpose(final M m, final int purpose) {
        toString = String.format(Locale.US, "%s/%d'", m, purpose);
        this.purpose = purpose;
    }

    public int getValue() {
        return purpose;
    }

    @NonNull
    @Override
    public String toString() {
        return toString;
    }

    public CoinType coinType(final int coinType) {
        return new CoinType(this, coinType);
    }
}