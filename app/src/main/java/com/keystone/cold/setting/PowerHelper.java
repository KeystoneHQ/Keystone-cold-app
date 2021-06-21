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

package com.keystone.cold.setting;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

public class PowerHelper {
    private static final String TAG = "Vault.PowerHelper";

    public static void setScreenOffTimeout(@NonNull Context context, int millisecond) {
        final ContentResolver contentResolver = context.getContentResolver();

        try {
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT, millisecond);
        } catch (Exception e) {
            Log.e(TAG, "Error in setBrightness", e);
        }
    }

    public static int getScreenOffTimeout(@NonNull Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final int defVal = 60000;

        try {
            return Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT, defVal);
        } catch (Exception e) {
            Log.e(TAG, "Error in setBrightness", e);
            return defVal;
        }
    }
}
