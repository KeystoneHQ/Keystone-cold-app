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

public class FileInfo {
    public final String fileName;
    public final String version;
    public final String md5;
    public final String sha1;
    public final String displayVersion;

    private FileInfo(String fileName, String version, String md5, String sha1, String displayVersion) {
        this.fileName = fileName;
        this.version = version;
        this.md5 = md5;
        this.sha1 = sha1;
        this.displayVersion = displayVersion;
    }

    static FileInfo from(@Nullable JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return null;
        } else {
            final String fileName = jsonObject.getString("fileName");
            final String version = jsonObject.getString("version");
            final String md5 = jsonObject.getString("md5");
            final String sha1 = jsonObject.getString("sha1");
            final String displayVersion = jsonObject.optString("display_version","");

            return new FileInfo(fileName, version, md5, sha1, displayVersion);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", version='" + version + '\'' +
                ", md5='" + md5 + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", displayVersion='" + displayVersion + '\'' +
                '}';
    }

}
