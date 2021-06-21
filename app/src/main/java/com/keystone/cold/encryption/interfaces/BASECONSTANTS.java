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

public abstract class BASECONSTANTS {

    public interface CONFIG {
        Digest DIGEST = Digest.MD5;
        int PAGE_SIZE = 1024;
        int SPEED = 57600;
    }

    public interface METHODS {
        int TEST = 0x0101;
        int GET_FIRMWARE_STATUS = 0x0103;
        int GET_FIRMWARE_PARAMETER = 0x0104;
        int RESET = 0x0306;
        int GET_RANDOM_ENTROPY = 0x0301;
        int WRITE_MNEMONIC = 0x0302;
        int VERIFY_MNEMONIC = 0x0502;
        int UPDATE_PASSPHRASE = 0x0303;
        int GET_EXTENDED_PUBLICKEY = 0x0305;
        int WEB_AUTH = 0x0701;
        int GET_UPDATE_KEY = 0x0702;
        int REQUEST_UPDATE = 0x0108;
        int WRITE_UPDATE_BYTES = 0x0106;
        int SIGN = 0x0307;
        int CHANGE_USER_PASSWORD = 0x0901;
        int RESET_USER_PASSWORD = 0x0902;
        int VERIFY_USER_PASSWORD = 0x0903;
        int FINGERPRINT_POLICY = 0x0904;
        int GET_MESSAGE = 0x0905;
        int REGISTER_PUBLIC_KEY = 0x0906;
        int VERIFY_FINGERPRINT = 0x0907;
        int CLEAR_TOKEN = 0x0908;
        int RESTART_SE = 0x0108;
        int GET_MASTER_FINGERPRINT = 0x0308;
    }

    public interface TAGS {
        int RESPONSE_CODE = 0x0002;
        int ERROR_MESSAGE = 0x0003;
        int FIRMWARE_STATUS = 0x0102;
        int FIRMWARE_SN = 0x0111;
        int FIRMWARE_APP_VERSION = 0x0106;
        int UPDATE_KEY_OPERATION = 0x0701;
        int UPDATE_KEY = 0x0702;
        int UPDATING_FLASH_POSITION = 0x0107;
        int UPDATING_PACKAGE_TYPE = 0x010A;
        int UPDATING_PACKAGE = 0x0109;
        int UPDATING_CHECKSUM = 0x010C;
        int UPDATING_SHA256 = 0x010B;
        int TX_HASH = 0x0307;
        int PUBLIC_KEY_HASH = 0x0303;
        int SIGNED = 0x0308;
        int ENTROPY = 0x0202;
        int ENTROPY_TYPE = 0x0201;
        int ENTROPY_CHECKSUM = 0x030b;
        int PATH = 0x0207;
        int EXTEND_PUB_KEY = 0x020A;
        int MNEMONIC = 0x0203;
        int ENCRYPTED = 0x0306;
        int SIGNATURE = 0x0308;
        int DECRYPTED = 0x0305;
        int PASSPHRASE = 0x0204;
        int APP_VERSION = 0x010f;
        int BOOT_VERSION = 0x0110;
        int CURVE = 0x030D;
        int NEW_PASSWORD = 0x0401;
        int CURRENT_PASSWORD = 0x0402;
        int PUBLIC_KEY = 0x0403;
        int AUTH_TOKEN = 0x0404;
        int NEED_TOKEN = 0x0405;
        int MESSAGE = 0x0406;
        int MESSAGE_SIGNATURE = 0x0407;
        int ACCESS_MODE = 0x0701;
        int FINGERPRINT_POLICY_TYPE = 0x0408;
        int FINGERPRINT_POLICY_VALUE = 0x0409;
        int WALLET_FLAG = 0x0210;
        int SLIP39_MASTER_SEED = 0x080B;
        int SLIP39_ID = 0x080C;
        int SLIP39_EXPONENT = 0x080D;
        int MASTER_FINGERPRINT = 0x020B;
    }

    public interface VALS {
        int SUCCESS_RESPONSE = 0x0000;
        int READ_UPDATE_KEY = 0x01;
        int UPDATE_FLASH_START_POSITION = 0x10000000;
    }
}