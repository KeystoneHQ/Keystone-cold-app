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

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class VibratorHelper {
    private static final String TAG = "Vault.VibratorHelper";
    private static final long milliseconds = 300L;

    public static void vibrate(@NonNull Context context) {

        final Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);

        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.e(TAG, "Cannot find vibrator");
            return;
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Vibrate permission has not gained");
            return;
        }

        try {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Exception e) {
            Log.e(TAG, "Error setting vibration", e);
        }
    }
}
