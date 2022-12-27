package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.helper.AddressManager;
import com.keystone.cold.remove_wallet_mode.helper.AddressNameConvertHelper;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;

import java.util.List;
import java.util.stream.Collectors;

public class AddressViewModel extends AndroidViewModel {

    private final String coinId;
    private final DataRepository repository;
    private final LiveData<List<AddressItem>> observableAddress;


    public AddressViewModel(@NonNull Application application, DataRepository repository, final String coinId) {
        super(application);
        this.coinId = coinId;
        this.repository = repository;
        observableAddress = Transformations.map(repository.loadAddress(coinId),
                input -> input.stream()
                        .map(AddressItem::new)
                        .peek(addressItem -> {
                            addressItem.setName(AddressNameConvertHelper.convertName(addressItem.getCoinId(), addressItem.getName()));
                        })
                        .collect(Collectors.toList()));
    }

    public LiveData<List<AddressItem>> getAddress() {
        return observableAddress;
    }


    public void updateAddress(AddressItem addressItem) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AddressEntity addressEntity = repository.loadAddressById(addressItem.getId());
            addressEntity.setName(addressItem.getName());
            repository.updateAddress(addressEntity);
        });
    }


    public LiveData<Boolean> addAddress(int count) {
        MutableLiveData<Boolean> status = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            AddressManager.addAddress(coinId, count, status);
        });
        return status;
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application application;
        private final String coinId;
        private final DataRepository repository;

        public Factory(@NonNull Application application, final String coinId) {
            this.application = application;
            this.coinId = coinId;
            repository = ((MainApplication) application).getRepository();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AddressViewModel(application, repository, coinId);
        }
    }
}
