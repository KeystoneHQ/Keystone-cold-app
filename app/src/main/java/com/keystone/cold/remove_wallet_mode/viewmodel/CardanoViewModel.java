package com.keystone.cold.remove_wallet_mode.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.setup.CardanoCreator;

import java.util.List;

public class CardanoViewModel extends AndroidViewModel {
    public static String SETUP_INITIAL = "cardano_setup_init";
    public static String SETUP_IN_PROCESS = "cardano_setup_in_process";
    public static String SETUP_SUCCESS = "cardano_setup_success";
    public static String SETUP_FAILED = "cardano_setup_failed";

    public MutableLiveData<String> getSetupStatus() {
        return setupStatus;
    }

    private final MutableLiveData<String> setupStatus = new MutableLiveData<>(SETUP_INITIAL);

    private final DataRepository repository;

    public CardanoViewModel(@NonNull Application application) {
        super(application);
        repository = ((MainApplication) application).getRepository();
    }

    public void setup(String password, String passphrase) {
        setupStatus.postValue(SETUP_IN_PROCESS);
        ADASetupManager adaSetupManager = ADASetupManager.getInstance();
        if (adaSetupManager.setupADARootKey(passphrase == null ? "" : passphrase, password)) {
            if (adaSetupManager.preSetupADAKeys(password)) {
                new CardanoCreator().setUp();
                setupStatus.postValue(SETUP_SUCCESS);
                return;
            }
        }
        setupStatus.postValue(SETUP_FAILED);
    }

    public MutableLiveData<Boolean> isAccountActive(int index) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coinEntity = repository.loadCoinSync(Coins.ADA.coinId());
            if (coinEntity != null) {
                List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
                AccountEntity accountEntity = accountEntities.get(index);
                if (accountEntity != null) {
                    result.postValue(accountEntity.getExPub() != null);
                    return;
                }
            }
            result.postValue(false);
        });
        return result;
    }
}
