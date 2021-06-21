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

package com.keystone.cold.encryption.interfaces;

import com.keystone.cold.update.utils.Digest;

public class CONSTANTS extends BASECONSTANTS {

    public interface CONFIG {
        Digest DIGEST = Digest.SHA256;
        int PAGE_SIZE = 528;
        int SPEED = 115200;
    }

    public interface METHODS extends BASECONSTANTS.METHODS {
        int GET_FIRMWARE_STATUS = 0x0102;
        int CHECK_UPDATE = 0x0201;
        int REQUEST_UPDATE = 0x0203;
        int WRITE_UPDATE_BYTES = 0x0202;
        int GET_FIRMWARE_PARAMETER = 0x0102;
    }

    public interface TAGS extends BASECONSTANTS.TAGS {
        int REQUEST_UPDATE_META_DATA = 0x010f;
        int REQUEST_UPDATE_FLAG = 0x0117;
    }

}
