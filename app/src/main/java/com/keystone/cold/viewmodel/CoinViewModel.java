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
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;

import java.util.List;

public class CoinViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private static LiveData<CoinEntity> mObservableCoin;
    private final LiveData<List<AddressEntity>> mObservableAddress;
    public final ObservableField<CoinEntity> coin = new ObservableField<>();

    private CoinViewModel(@NonNull Application application, DataRepository repository,
                          final String coinId) {
        super(application);
        mRepository = repository;
        mObservableCoin = repository.loadCoin(coinId);
        mObservableAddress = repository.loadAddress(coinId);

    }

    public LiveData<CoinEntity> getObservableCoin() {
        return mObservableCoin;
    }

    public LiveData<List<AddressEntity>> getAddress() {
        return mObservableAddress;
    }

    public void setCoin(CoinEntity coin) {
        this.coin.set(coin);
    }

    public void updateAddress(AddressEntity addr) {
        AppExecutors.getInstance().diskIO().execute(() -> mRepository.updateAddress(addr));
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;
        private final String mCoinId;
        private final DataRepository mRepository;

        public Factory(@NonNull Application application, String coinId) {
            mApplication = application;
            mCoinId = coinId;
            mRepository = ((MainApplication) application).getRepository();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new CoinViewModel(mApplication, mRepository, mCoinId);
        }
    }
}
