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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.callables.VerifyMnemonicCallable;

public class SharedDataViewModel extends AndroidViewModel {

    private final MutableLiveData<String> scanResult = new MutableLiveData<>();

    public SharedDataViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getScanResult() {
        return scanResult;
    }

    public void updateScanResult(String s) {
        scanResult.setValue(s);
    }

    public boolean verifyMnemonic(String mnemonic) {
        return new VerifyMnemonicCallable(mnemonic, null, 0).call();
    }
}
