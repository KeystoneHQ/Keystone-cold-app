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

package com.keystone.cold.selfcheck;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.keystone.cold.BuildConfig;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.BlockingCallable;
import com.keystone.cold.encryption.exception.EncryptionCoreException;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;


public class SecurityCheck {
    private static final String TAG = "Vault.SecurityCheck";

    public static final int RESULT_OK = 0;
    public static final int RESULT_UNDER_ATTACK = -1;

    private static final int CODE_SYS_OK = 0x00;
    private static final int CODE_SYS_ATTACKED = 0x01;
    private static final int CODE_FW_OK = 0x0000;
    public static final int CODE_FW_GET_STATUS_FAILED = 0x0100;
    private static final int CODE_FW_STATUS_NOT_FOUND = 0x0200;
    private static final int CODE_FW_STATUS_ATTACKED = 0x0300;
    private static final int CODE_FW_ERT_ATTACKED = 0x0400;
    public static final int CODE_FW_IN_BOOTMODE = 0x0500;
    public static final int CODE_STATUS_MIS_MATCH = 0x0600;
    public static final int CODE_STATUS_RUNTIME_INVALID = 0x1000;

    public CheckResult doSelfCheck(AppCompatActivity context) {
        Log.i(TAG, "start self checking...");

        if (BuildConfig.DEBUG || "simulator".equals(BuildConfig.FLAVOR)) {
            Log.i(TAG, "downgrade security check result in debug mode");
            return new CheckResult(0, 0, 0, 0);
        }

        int firmwareCode = checkFirmwareStatus(context);
        int systemCode = checkSystemStatus(context);
        int signatureCode = SysChecker.check(context);

        Log.i(TAG, "firmware status: " + firmwareCode);
        Log.i(TAG, "system status: " + systemCode);
        Log.i(TAG, "signature check: " + signatureCode);

        int result = RESULT_OK;
        if ((firmwareCode & 0xFF00) != CODE_FW_OK || systemCode != CODE_SYS_OK || signatureCode != SysChecker.CODE_SIG_OK) {
            result = RESULT_UNDER_ATTACK;
        }

        return new CheckResult(result, firmwareCode, systemCode, signatureCode);
    }

    public CheckResult attackChecking(Context context) {
        int firmwareCode = checkFirmwareStatus();
        int systemCode = checkSystemStatus(context);
        int signatureCode = SysChecker.check(context);

        int result = RESULT_OK;
        if ((firmwareCode & 0xFF00) != CODE_FW_OK || systemCode != CODE_SYS_OK || signatureCode != SysChecker.CODE_SIG_OK) {
            result = RESULT_UNDER_ATTACK;
        }

        return new CheckResult(result,firmwareCode,systemCode,signatureCode);
    }

    private int checkSystemStatus(Context context) {
        Log.i(TAG, "check system status");
        try {
            boolean rooted = new RootChecker(context).isDeviceRooted();
            return rooted ? CODE_SYS_ATTACKED : CODE_SYS_OK;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return CODE_SYS_OK;
        }
    }

    private int checkFirmwareStatus(AppCompatActivity context) {
        Log.i(TAG, "check firmware status");
        try {
            final Callable<Packet> callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.GET_FIRMWARE_STATUS).setRetryTimes(2).build());
            final Packet result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.FIRMWARE_STATUS);

            if (payload != null) {
                int status = payload.toInt() & 0xFF;
                if (status == 0xA0){
                    return (CODE_FW_STATUS_ATTACKED | status);
                } else if(!isWalletStatusMatch(status, context)) {
                    return (CODE_STATUS_MIS_MATCH | status);
                } else {
                    return (CODE_FW_OK | status);
                }
            } else {
                // has not 0102 tag ,is in boot mode
                return CODE_FW_IN_BOOTMODE;
            }
        } catch (EncryptionCoreException e) {
            Log.e(TAG, e.toString());
            if (e.getErrorCode() == 0xFFAA) {
                return CODE_FW_ERT_ATTACKED;
            }
            return CODE_FW_GET_STATUS_FAILED | (e.getErrorCode() & 0xFF);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return CODE_FW_GET_STATUS_FAILED;
        }
    }

    private boolean isWalletStatusMatch(int status, Context context) {
        return ((status == 0xCB || status == 0) && !Utilities.hasVaultCreated(context))
                ||
                (status == 0x88 && Utilities.hasVaultCreated(context));
    }

    private int checkFirmwareStatus() {
        Log.i(TAG, "check firmware status");
        try {
            final Callable<Packet> callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.GET_FIRMWARE_STATUS).setRetryTimes(2).build());
            final Packet result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.FIRMWARE_STATUS);

            if (payload != null) {
                int status = payload.toInt() & 0xFF;
                return (status == 0xA0) ? (CODE_FW_STATUS_ATTACKED | status) : (CODE_FW_OK | status);
            } else {
                // has not 0102 tag ,is in boot mode
                return CODE_FW_OK;
            }
        } catch (EncryptionCoreException e) {
            Log.e(TAG, e.toString());
            if (e.getErrorCode() == 0xFFAA) {
                return CODE_FW_ERT_ATTACKED;
            }
            return CODE_FW_GET_STATUS_FAILED | (e.getErrorCode() & 0xFF);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return CODE_FW_GET_STATUS_FAILED;
        }
    }

    public static class CheckResult {
        public final int result;
        public final int firmwareStatusCode;
        public final int systemStatusCode;
        public final int signatureStatusCode;

        CheckResult(int result, int firmwareStatusCode, int systemStatusCode, int signatureCode) {
            this.result = result;
            this.firmwareStatusCode = firmwareStatusCode;
            this.systemStatusCode = systemStatusCode;
            this.signatureStatusCode = signatureCode;
        }

        @NonNull
        @Override
        public String toString() {
            return "CheckResult{" +
                    "result=" + result +
                    ", firmwareStatusCode=" + firmwareStatusCode +
                    ", systemStatusCode=" + systemStatusCode +
                    ", signatureStatusCode=" + signatureStatusCode +
                    '}';
        }
    }
}

