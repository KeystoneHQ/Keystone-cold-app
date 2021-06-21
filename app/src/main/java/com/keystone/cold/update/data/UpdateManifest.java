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

package com.keystone.cold.update.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateManifest {
    public FileInfo serial;
    public FileInfo app;
    public FileInfo system;
    public String sha256;

    private UpdateManifest(FileInfo serial, FileInfo app, FileInfo system) {
        this.serial = serial;
        this.app = app;
        this.system = system;
    }

    @Nullable
    public static UpdateManifest from(@Nullable JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return null;
        } else {
            final FileInfo serial = jsonObject.has("serial") ? FileInfo.from(jsonObject.getJSONObject("serial")) : null;
            final FileInfo app = jsonObject.has("app") ? FileInfo.from(jsonObject.getJSONObject("app")) : null;
            final FileInfo system = jsonObject.has("system") ? FileInfo.from(jsonObject.getJSONObject("system")) : null;

            return new UpdateManifest(serial, app, system);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "UpdateManifest{" +
                "serial=" + serial +
                ", app=" + app +
                ", system=" + system +
                '}';
    }
}
