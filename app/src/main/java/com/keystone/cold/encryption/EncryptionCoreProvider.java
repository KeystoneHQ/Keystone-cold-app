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
import android.os.Looper;

import androidx.annotation.NonNull;

import com.keystone.cold.encryption.exception.EncryptionCoreException;
import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;
import com.keystone.cold.encryptioncore.base.Payload;
import com.keystone.cold.encryptioncore.interfaces.Callback;
import com.keystone.cold.encryptioncore.interfaces.JobScheduler;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.encryptioncore.utils.Preconditions;

public class EncryptionCoreProvider {
    private static final EncryptionCoreProvider sInstance = new EncryptionCoreProvider();
    private JobScheduler mImpl;

    @NonNull
    public static EncryptionCoreProvider getInstance() {
        return sInstance;
    }

    static void checkRawResponse(int target, @NonNull Packet result) {
        Preconditions.checkNotNull(result);
        Preconditions.checkArgument(result.getId() == target,
                String.format("id is not the same, expected %s but actual is %s",
                        ByteFormatter.addHexPrefix(target),
                        ByteFormatter.addHexPrefix(result.getId())));

        final Payload responseCodePayload = result.getPayload(CONSTANTS.TAGS.RESPONSE_CODE);
        Preconditions.checkNotNull(responseCodePayload,
                "can not find response code");

        final int responseCode = responseCodePayload.toInt();
        if (responseCode != CONSTANTS.VALS.SUCCESS_RESPONSE) {
            final Payload errorMessagePayload = result.getPayload(CONSTANTS.TAGS.ERROR_MESSAGE);
            final String errorMessage;

            if (errorMessagePayload != null) {
                errorMessage = errorMessagePayload.toUtf8();
            } else {
                errorMessage = null;
            }

            throw new EncryptionCoreException(responseCode, errorMessage);
        }
    }

    public void initialize(@NonNull Context context) {
        Preconditions.checkState(Looper.getMainLooper() == Looper.myLooper(), "should initialize in main loop");
        Preconditions.checkState(mImpl == null, "should not initialize again");
        mImpl = new JobSchedulerWrapper(new ImplementProvider().getImplement(context));
    }

    @NonNull
    public JobScheduler getImpl() {
        return Preconditions.checkNotNull(mImpl, "should initialize first");
    }

    private static class JobSchedulerWrapper implements JobScheduler {
        private final JobScheduler mBase;

        private JobSchedulerWrapper(@NonNull JobScheduler base) {
            mBase = Preconditions.checkNotNull(base);
        }

        @Override
        public void offer(@NonNull Packet packet, @NonNull Callback callback) {
            mBase.offer(packet, new CallbackWrapper(packet.getId(), callback));
        }
    }

    private static class CallbackWrapper implements Callback {
        private final int mId;
        private final Callback mBase;

        private CallbackWrapper(int id, @NonNull Callback base) {
            mId = id;
            mBase = Preconditions.checkNotNull(base);
        }

        @Override
        public void onSuccess(@NonNull Packet packet) {

            try {
                checkRawResponse(mId, packet);
            } catch (Exception e) {
                onFail(e);
                return;
            }

            mBase.onSuccess(packet);
        }

        @Override
        public void onFail(@NonNull Exception exception) {
            mBase.onFail(exception);
        }
    }

}
