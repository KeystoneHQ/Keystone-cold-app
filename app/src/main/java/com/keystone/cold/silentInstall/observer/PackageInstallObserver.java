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

package com.keystone.cold.silentInstall.observer;

import android.content.pm.IPackageInstallObserver;

import com.keystone.cold.silentInstall.constants.InstallConstants;
import com.keystone.cold.silentInstall.executor.MainExecutor;
import com.keystone.cold.silentInstall.listener.InstallResultListener;


public class PackageInstallObserver extends IPackageInstallObserver.Stub {
    private final InstallResultListener installResultListener;

    public PackageInstallObserver(MainExecutor mainExecutor, InstallResultListener installResultListener) {

        this.installResultListener = installResultListener;
    }

    @Override
    public void packageInstalled(final String packageName, int returnCode) {

        if (installResultListener == null) {
            return;
        }
        if (checkInstallSuccess(returnCode)) {
            success(packageName);
        } else {
            failure(packageName);
        }
    }

    public void success(final String packageName) {
        installResultListener.installSuccess(packageName);
    }

    public void failure(final String packageName) {
        installResultListener.installFailure(packageName);
    }

    private boolean checkInstallSuccess(int returnCode) {
        return returnCode == InstallConstants.INSTALL_SUCCEEDED;
    }
}
