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

package com.keystone.cold.encryption;

import android.content.Context;

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.EncryptionCore;
import com.keystone.cold.encryptioncore.base.Config;
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;

class ImplementProvider {
    private static boolean isInitialize = false;

    JobScheduler getImplement(@NonNull Context context) {
        if (!isInitialize) {
            isInitialize = true;

            initializeCore(context);
        }

        return EncryptionCore.getInstance();
    }

    private void initializeCore(@NonNull Context context) {
        final Config config = new Config.Builder()
                .setPortSpeed(CONSTANTS.CONFIG.SPEED)
                .build();

        EncryptionCore.initialize(context, config);
    }
}