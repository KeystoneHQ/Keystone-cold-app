package com.keystone.cold.remove_wallet_mode.viewmodel;

import static com.keystone.cold.util.URRegistryHelper.getPathComponents;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.cryptocore.CardanoService;
import com.keystone.cold.cryptocore.RCCService;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.remove_wallet_mode.helper.setup.CardanoCreator;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static String getXPubByPath(String path, DataRepository repository) {
        CoinEntity ada = repository.loadCoinSync(Coins.ADA.coinId());
        List<AccountEntity> accounts = repository.loadAccountsForCoin(ada);
        Optional<AccountEntity> target = accounts.stream().filter(accountEntity -> path.toUpperCase().startsWith(accountEntity.getHdPath().toUpperCase())).findAny();
        String result = null;
        if (target.isPresent()) {
            result = target.get().getExPub();
        }
        return result;
    }

    public static AccountEntity getAccountByPath(String path, DataRepository repository) {
        CoinEntity ada = repository.loadCoinSync(Coins.ADA.coinId());
        List<AccountEntity> accounts = repository.loadAccountsForCoin(ada);
        Optional<AccountEntity> target = accounts.stream().filter(accountEntity -> path.toUpperCase().startsWith(accountEntity.getHdPath().toUpperCase())).findAny();
        return target.orElse(null);
    }

    public void checkAddressOrAdd(int accountIndex) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity ada = repository.loadCoinSync(Coins.ADA.coinId());
            List<AccountEntity> accounts = repository.loadAccountsForCoin(ada);
            AccountEntity accountEntity = accounts.get(accountIndex);
            if (accountEntity.getAddressLength() == 0) {
                addAddress(accountEntity);
            }
        });
    }

    public static LiveData<List<AddressItem>> filterAddressByAccount(LiveData<List<AddressItem>> addressItems, int accountIndex) {
        return Transformations.map(addressItems,
                input -> input.stream().filter(addressItem -> addressItem.getPath().startsWith(Coins.ADA.getAccounts()[accountIndex])).collect(Collectors.toList())
        );
    }

    public void addAddress(int accountIndex) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            addAddress(getAccountByIndex(accountIndex));
        });
    }

    public AccountEntity getAccountByIndex(int accountIndex) {
        CoinEntity coinEntity = repository.loadCoinSync(Coins.ADA.coinId());
        List<AccountEntity> accountEntities = repository.loadAccountsForCoin(coinEntity);
        return accountEntities.get(accountIndex);
    }

    public void addAddress(AccountEntity accountEntity) {
        int index = accountEntity.getAddressLength();
        //type 0: base, 1: stake
        String address = CardanoService.deriveAddress(accountEntity.getExPub(), index, 0);
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setName(Coins.ADA.coinCode() + "-" + (index + 1));
        addressEntity.setIndex(index);
        addressEntity.setAddressString(address);
        addressEntity.setCoinId(Coins.ADA.coinId());
        addressEntity.setBelongTo(repository.getBelongTo());
        String path = accountEntity.getHdPath() + "/0/" + index;
        addressEntity.setPath(path);
        addressEntity.setDisplayName(Coins.ADA.coinCode() + "-" + (index + 1));
        repository.insertAddress(addressEntity);
        accountEntity.setAddressLength(index + 1);
        repository.updateAccount(accountEntity);
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

    public LiveData<String[]> getStakeAddress(int accountIndex, int addressIndex) {
        MutableLiveData<String[]> address = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            AccountEntity accountEntity = getAccountByIndex(accountIndex);
            String stakeAddress = CardanoService.deriveAddress(accountEntity.getExPub(), addressIndex, 1);
            String enterpriseAddress = CardanoService.deriveAddress(accountEntity.getExPub(), addressIndex, 2);
            String[] result = new String[]{stakeAddress, enterpriseAddress};
            address.postValue(result);
        });
        return address;
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
