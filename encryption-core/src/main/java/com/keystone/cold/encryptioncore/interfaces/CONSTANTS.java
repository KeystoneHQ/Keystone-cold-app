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

package com.keystone.cold.encryptioncore.interfaces;

public interface CONSTANTS {
    byte STX = 0x02;
    byte ETX = 0x03;
    int MAX_BODY_SIZE = 2048;
    int MIN_BUFFER_SIZE = 1 /*stx*/ + 1/*encryption*/ + 2 /*length*/ + 1 /*etx*/ + 1 /*lrc*/;
    int MAX_BUFFER_SIZE = MIN_BUFFER_SIZE + MAX_BODY_SIZE;

    interface TAGS {
        int PACKET_ID = 0x01;
        int ENCRYPTION_STATUS = 0x04;
    }

    interface ENCRYPTION_STATUS {
        int DISABLED = 0x00;
        int ENABLED = 0x01;
    }
}
