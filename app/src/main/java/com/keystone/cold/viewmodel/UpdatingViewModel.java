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

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.sdcard.OnSdcardStatusChange;
import com.keystone.cold.sdcard.SdCardStatusMonitor;
import com.keystone.cold.update.Checking;
import com.keystone.cold.update.Updating;
import com.keystone.cold.update.data.UpdateManifest;
import com.keystone.cold.update.utils.Digest;
import com.keystone.cold.update.utils.Storage;

import org.spongycastle.util.encoders.Hex;

public class UpdatingViewModel extends AndroidViewModel {

    @SuppressLint("StaticFieldLeak")
    private final Context mContext;

    private final MutableLiveData<UpdateManifest> updateManifest = new MutableLiveData<>();
    private final MutableLiveData<UpdatingState> updatingState = new MutableLiveData<>();

    public static final int MIN_BATTERY_FOR_UPDATE = 70;

    public MutableLiveData<UpdateManifest> getUpdateManifest() {
        return updateManifest;
    }

    public MutableLiveData<UpdatingState> getUpdatingState() {
        return updatingState;
    }

    public UpdatingViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
        checkUpdate();
        SdCardStatusMonitor.getInstance((MainApplication) application).register(new OnSdcardStatusChange() {
            @Override
            public String id() {
                return "updateChecking";
            }

            @Override
            public void onInsert() {
                checkUpdate();
            }

            @Override
            public void onRemove() {
                updateManifest.setValue(null);
            }
        });
    }

    private void checkUpdate() {
        final Storage storage = Storage.createByEnvironment();
        if (storage != null) {
            AppExecutors.getInstance().networkIO().execute(() -> {
                try {
                    UpdateManifest manifest = new Checking(storage).call();
                    if (manifest != null) {
                        manifest.sha256 = Hex.toHexString(Digest.SHA256.checksum(storage.getUpdateZipFile()));
                    }
                    updateManifest.postValue(manifest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public void doUpdate(String password) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            final Storage storage = Storage.createByEnvironment();
            if (storage == null) {
                return;
            }
            UpdateManifest manifest = updateManifest.getValue();

            if (manifest == null) {
                return;
            }
            try {
                updatingState.postValue(UpdatingState.UPDATING);
                final boolean success = new Updating(storage, mContext, manifest, password).call();

                if (success) {
                    updatingState.postValue(UpdatingState.UPDATING_SUCCESS);
                } else {
                    updatingState.postValue(UpdatingState.UPDATING_FAILED);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public enum UpdatingState {
        UPDATING,
        UPDATING_SUCCESS,
        UPDATING_FAILED,
    }

}
