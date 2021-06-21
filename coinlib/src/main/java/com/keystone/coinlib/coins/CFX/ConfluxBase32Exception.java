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

package com.keystone.coinlib.coins.CFX;

class ConfluxBase32Exception extends RuntimeException {
    public static final String INVALID_BASE32_STR = "include invalid char";
    public static final String INVALID_BASE32_WORDS = "word should in range [0-31]";

    private String reason;

    public ConfluxBase32Exception(String reason) {
        super(String.format("Conflux base32 exception: (%s)", reason));
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
