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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.VerifyMnemonicCallable;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.WhiteListEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhiteListModel extends AndroidViewModel {

    private final MediatorLiveData<List<WhiteListEntity>> mObservableList;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;
    private final MediatorLiveData<Boolean> verifyMnemonic = new MediatorLiveData<>();
    private final DataRepository repo;

    public WhiteListModel(@NonNull Application application) {
        super(application);

        mObservableList = new MediatorLiveData<>();
        mObservableCoins = new MediatorLiveData<>();
        mObservableList.setValue(null);
        mObservableCoins.setValue(null);
        verifyMnemonic.setValue(false);
        repo = MainApplication.getApplication().getRepository();
        mObservableList.addSource(repo.loadWhiteList(),
                entities -> mObservableList.setValue(filterByBelongTo(entities)));
        mObservableCoins.addSource(repo.loadCoins(), mObservableCoins::setValue);
    }

    private List<WhiteListEntity> filterByBelongTo(List<WhiteListEntity> entities) {
        String passphraseHash = Utilities.getCurrentBelongTo(getApplication());
        return entities.isEmpty() ? Collections.emptyList()
                :
                entities.stream()
                        .filter(entity -> passphraseHash.equals(entity.getBelongTo()))
                        .collect(Collectors.toList());
    }

    public LiveData<List<WhiteListEntity>> getWhiteList() {
        return mObservableList;
    }

    public void deleteWhiteList(WhiteListEntity entity) {
        AppExecutors.getInstance().diskIO().execute(() -> repo.deleteWhiteList(entity));
    }

    public void insertWhiteList(WhiteListEntity entity) {
        AppExecutors.getInstance().diskIO().execute(() -> repo.insertWhiteList(entity));
    }

    public LiveData<List<CoinEntity>> getSupportCoins() {
        return mObservableCoins;
    }

    public void verifyMnemonic(String mnemonic) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean match = new VerifyMnemonicCallable(mnemonic, null, 0).call();
            verifyMnemonic.postValue(match);
        });
    }

    public MediatorLiveData<Boolean> getVerifyMnemonic() {
        return verifyMnemonic;
    }
}
