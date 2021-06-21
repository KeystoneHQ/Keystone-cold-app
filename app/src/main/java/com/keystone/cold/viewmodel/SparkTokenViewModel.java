/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;

public class SparkTokenViewModel extends AndroidViewModel {

    public static final String XPUB_PATH = "m/44'/554'/0'";

    public SparkTokenViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getAddress(int index) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            String xpub = new GetExtendedPublicKeyCallable(XPUB_PATH).call();
            String address = new Eth.Deriver().derive(xpub, 0, index);
            liveData.postValue(address);
        });
        return liveData;
    }
}
