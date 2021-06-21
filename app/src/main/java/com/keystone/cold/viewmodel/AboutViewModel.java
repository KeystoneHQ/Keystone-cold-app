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

package com.keystone.cold.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.FirmwareParameterCallable;

public class AboutViewModel extends AndroidViewModel {
    private final ObservableField<String> sn = new ObservableField<>("");
    private final ObservableField<String> firmwareAppVersion = new ObservableField<>("");
    private final ObservableField<String> sysVersion = new ObservableField<>("");
    private final ObservableField<String> appVersion = new ObservableField<>("");

    public AboutViewModel(@NonNull Application application) {
        super(application);
        getFirmwareInfo();
        getSystemVersion();
        getAppVersion(application);
    }

    public ObservableField<String> getSn() {
        return sn;
    }

    public ObservableField<String> getFirmwareAppVersion() {
        return firmwareAppVersion;
    }

    public ObservableField<String> getSysVersion() {
        return sysVersion;
    }

    public ObservableField<String> getAppVersion() {
        return appVersion;
    }

    private void getFirmwareInfo() {
        sn.set(SystemProperties.get("persist.sys.serialno"));
        AppExecutors.getInstance().diskIO().execute(() -> {
            String res = new FirmwareParameterCallable().call();
            if (res == null) {
                return;
            }
            firmwareAppVersion.set(res);
        });
    }

    private void getSystemVersion() {
        sysVersion.set(SystemProperties.get("ro.build.display.id"));
    }

    private void getAppVersion(Context context) {
        String version = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        appVersion.set(version);
    }
}
