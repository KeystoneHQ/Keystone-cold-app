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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class SysChecker {
    private static final String TAG = "Vault.SysCheck";
    private static final String FRAMEWORK_PATH = "/system/framework/framework-res.apk";
    public static final int CODE_SIG_OK = 0x0;
    public static final int CODE_SIG_PACKAGE_NOT_FOUND = 0x01;
    public static final int CODE_SIG_NOT_FOUND = 0x02;
    public static final int CODE_SIG_NOT_MATCH = 0x03;

    public static int check(Context context) {
        try {
            PackageInfo sys = context.getPackageManager().getPackageArchiveInfo(
                    FRAMEWORK_PATH,
                    PackageManager.GET_SIGNATURES);
            PackageInfo app = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);

            if ((sys.signatures != null && sys.signatures.length > 0)
                    && (app.signatures != null && app.signatures.length > 0)) {
                if (sys.signatures[0].equals(app.signatures[0])) {
                    return CODE_SIG_OK;
                } else {
                    return CODE_SIG_NOT_MATCH;
                }
            } else {
                return CODE_SIG_NOT_FOUND;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
            return CODE_SIG_PACKAGE_NOT_FOUND;
        }
    }
}
