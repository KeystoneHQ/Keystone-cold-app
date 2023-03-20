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

package com.keystone.cold.remove_wallet_mode.viewmodel.record;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.model.Tx;
import com.keystone.cold.remove_wallet_mode.helper.TxRecordManager;

import java.util.List;


public class TxRecordViewModel extends AndroidViewModel {

    public TxRecordViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Tx>> loadTxs(String coinId) {
        MutableLiveData<List<Tx>> txs = new MutableLiveData<>();
        TxRecordManager.loadTx(coinId, txs);
        return txs;
    }
}
