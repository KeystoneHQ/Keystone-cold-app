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

package com.keystone.cold.encryptioncore.job;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.interfaces.Callback;
import com.keystone.cold.encryptioncore.interfaces.Cipher;
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;
import com.keystone.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.keystone.cold.encryptioncore.utils.Preconditions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobSchedulerImpl implements JobScheduler {
    private final SerialManagerProxy mManager;
    private final ExecutorService mExecutor;
    private final Cipher mCipher;

    public JobSchedulerImpl(@NonNull SerialManagerProxy manager, @Nullable Cipher cipher) {
        mManager = Preconditions.checkNotNull(manager);
        mCipher = cipher;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void offer(@NonNull Packet packet, @NonNull Callback callback) {
        mExecutor.submit(new Job(mManager, new PackerImpl(mCipher), packet, callback));
    }
}
