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

package com.keystone.cold.encryptioncore.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.keystone.cold.encryptioncore.utils.Preconditions;

public class Config {
    @Nullable
    public final Pair<byte[], byte[]> secretKey;
    public final int portSpeed;

    private Config(@Nullable Pair<byte[], byte[]> secretKey, int portSpeed) {
        this.secretKey = secretKey;
        this.portSpeed = portSpeed;
    }

    public static class Builder {
        private Pair<byte[], byte[]> mKey;
        private int mPortSpeed;

        public Builder setSecretKey(@NonNull byte[] desKey, @NonNull byte[] desKeyIv) {
            Preconditions.checkNotNull(desKey);
            Preconditions.checkNotNull(desKeyIv);
            mKey = Pair.create(desKey, desKeyIv);

            return this;
        }

        public Builder setPortSpeed(int portSpeed) {
            this.mPortSpeed = portSpeed;
            return this;
        }

        public Config build() {
            return new Config(mKey, mPortSpeed);
        }
    }
}
