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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class BrightnessHelper {
    private static final String TAG = "Vault.BrightnessHelper";

    public static void setManualMode(@NonNull Context context) {
        final ContentResolver contentResolver = context.getContentResolver();

        try {
            final int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setManualMode", e);
        }
    }

    /**
     * @return the value of brightness between 0~255
     */
    public static int getBrightness(@NonNull Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final int defVal = 125;

        try {
            return Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, defVal);
        } catch (Exception e) {
            Log.e(TAG, "Error in getBrightness", e);
        }

        return defVal;
    }

    public static void setBrightness(@NonNull Context context, @IntRange(from = 0, to = 255) int brightness) {
        final ContentResolver contentResolver = context.getContentResolver();

        try {
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        } catch (Exception e) {
            Log.e(TAG, "Error in setBrightness", e);
        }
    }
}
