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

public class AddressException extends RuntimeException {

    private static final long serialVersionUID = 2338294090416527939L;

    public static final String INVALID_PREFIX = "HEX prefix 0x missed";
    public static final String INVALID_LENGTH = "wrong length";
    public static final String INVALID_TYPE = "wrong type";
    public static final String INVALID_HEX = "wrong HEX format";

    private String reason;

    public AddressException() {
    }

    public AddressException(String reason) {
        super(String.format("invalid address (%s)", reason));

        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
