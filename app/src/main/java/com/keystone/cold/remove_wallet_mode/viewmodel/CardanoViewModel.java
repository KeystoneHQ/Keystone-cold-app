package com.keystone.cold.remove_wallet_mode.viewmodel;

import static com.keystone.cold.util.URRegistryHelper.getPathComponents;

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
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public static void pureSetup(String password, String passphrase) {
        ADASetupManager adaSetupManager = ADASetupManager.getInstance();
        if (adaSetupManager.setupADARootKey(passphrase == null ? "" : passphrase, password)) {
            if (adaSetupManager.preSetupADAKeys(password)) {
                new CardanoCreator().setUp();
            }
        }
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

    public static boolean isCardanoPath(String path) {
        return Arrays.stream(Coins.ADA.getAccounts()).anyMatch(v -> v.equalsIgnoreCase(path));
    }

    public static void checkOrSetup(String path, String password, DataRepository repository) {
        String[] pieces = path.split("/");
        int account = Integer.parseInt(pieces[3].replace("'", ""));
        if (!isAccountActive(account, repository)) {
            // user won't call this method with a passphrase
            pureSetup(password, "");
        }
    }

    public static byte[] getXPub(String path, DataRepository repository) {
        CoinEntity ada = repository.loadCoinSync(Coins.ADA.coinId());
        List<AccountEntity> accounts = repository.loadAccountsForCoin(ada);
        Optional<AccountEntity> target = accounts.stream().filter(accountEntity -> accountEntity.getHdPath().equalsIgnoreCase(path)).findAny();
        byte[] result = new byte[]{};
        if (target.isPresent()) {
            String xpub = target.get().getExPub();
            result = Hex.decode(xpub);
        }
        return result;
    }

    public static Boolean isAccountActive(int index, DataRepository repository) {
        CoinEntity coinEntity = repository.loadCoinSync(Coins.ADA.coinId());
        if (coinEntity != null) {
            List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
            AccountEntity accountEntity = accountEntities.get(index);
            if (accountEntity != null) {
                return accountEntity.getExPub() != null;
            }
        }
        return false;
    }
}
