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

/**
 * Represents the first part of a CoinPath path. To create, use the static factory method {@link CoinPath#m()}.
 * m / purpose' / coin_type' / account' / change / address_index
 */
public final class M {

    private final Purpose PURPOSE_44;
    private final Purpose PURPOSE_49;


    private final String prefix;

    public M(String prefix) {
        this.prefix = prefix;
        PURPOSE_44 = new Purpose(this, 44);
        PURPOSE_49 = new Purpose(this, 49);
    }

    public Purpose purpose(final int purpose) {
        switch (purpose) {
            case 44:
                return PURPOSE_44;
            case 49:
                return PURPOSE_49;
            default:
                return new Purpose(this, purpose);
        }
    }

    public Purpose purpose44() {
        return PURPOSE_44;
    }

    public Purpose purpose49() {
        return PURPOSE_49;
    }

    @NonNull
    @Override
    public String toString() {
        return prefix;
    }
}