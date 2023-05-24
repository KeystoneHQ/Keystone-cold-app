package com.keystone.cold.integration.chains;

import static com.keystone.coinlib.utils.Coins.isDefaultOpen;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.cryptocore.RCCService;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.encryption.EncryptionCoreProvider;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveCryptoAccount;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

public class ArweaveViewModel extends AndroidViewModel {
    private static final String TAG = "ArweaveViewModel";
    private AuthenticateModal.OnVerify.VerifyToken token;
    private final DataRepository mRepo;
    private final Context mContext;
    private final MutableLiveData<Boolean> generatingAddress = new MutableLiveData<>(null);

    public ArweaveViewModel(@NonNull Application application) {
        super(application);
        mRepo = MainApplication.getApplication().getRepository();
        mContext = application.getApplicationContext();
    }

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    private void clearToken() {
        this.token = null;
    }

    private CoinEntity getARCoin() {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(Coins.AR.coinId());
        entity.setName(Coins.AR.coinName());
        entity.setCoinCode(Coins.AR.coinCode());
        entity.setIndex(Coins.AR.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(mContext));
        entity.setAddressCount(0);
        entity.setShow(isDefaultOpen(Coins.AR.coinCode()));

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setHdPath(Coins.AR.getAccounts()[0]);
        entity.addAccount(accountEntity);
        return entity;
    }

    public MutableLiveData<Boolean> getGeneratingAddress() {
        return generatingAddress;
    }

    public LiveData<Boolean> hasArweaveAddress() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coin = mRepo.loadCoinSync(Coins.AR.coinId());
            if (coin == null) {
                result.postValue(false);
            } else {
                List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coin);
                if (accounts.size() == 0) {
                    result.postValue(false);
                    return;
                }
                List<AddressEntity> addresses = mRepo.loadAddressSync(coin.getCoinId());
                result.postValue(addresses.size() != 0);
            }
        });
        return result;
    }

    public void getRSAPublicKey() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            generatingAddress.postValue(true);
            boolean isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
            String portName = EncryptionCoreProvider.getInstance().getPortName();
            String result = RCCService.getRSAPublicKey(new RCCService.Passport(token.password, isMainWallet, portName));
            if (result == null) {
                //temporary fix for not getting RSA pubkey
                generatingAddress.postValue(false);
            }
            else {
                addArweaveAddressToDB(result);
            }
        });
    }

    public static String getARPublicKey() {
        DataRepository mRepo = MainApplication.getApplication().getRepository();
        try {
            CoinEntity coin = mRepo.loadCoinSync(Coins.AR.coinId());
            if (coin == null) {
                return null;
            }
            List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coin);
            if (accounts.size() == 0) {
                return null;
            }
            AccountEntity accountEntity = accounts.get(0);
            if (accountEntity.getAddition() == null) {
                return null;
            }
            JSONObject addition = new JSONObject(accountEntity.getAddition());
            return addition.getString("public_key");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getARAddress() {
        DataRepository mRepo = MainApplication.getApplication().getRepository();
        CoinEntity coin = mRepo.loadCoinSync(Coins.AR.coinId());
        if (coin == null) {
            return null;
        }
        List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coin);
        if (accounts.size() == 0) {
            return null;
        }
        List<AddressEntity> addresses = mRepo.loadAddressSync(coin.getCoinId());
        return addresses.get(0).getAddressString();
    }

    private void addArweaveAddressToDB(String publicKey) {
        try {
            CoinEntity coin = mRepo.loadCoinSync(Coins.AR.coinId());
            if (coin == null) {
                coin = getARCoin();
                mRepo.insertCoin(coin);
                coin = mRepo.loadCoinSync(Coins.AR.coinId());
            }
            List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coin);
            if (accounts.size() == 0) {
                AccountEntity accountEntity = new AccountEntity();
                accountEntity.setHdPath(Coins.AR.getAccounts()[0]);
                accountEntity.setCoinId(coin.getId());
                JSONObject object = new JSONObject();
                object.put("public_key", publicKey);
                accountEntity.setAddition(object.toString());
                AddressEntity addressEntity = getArweaveAddress(publicKey);
                coin.setAddressCount(1);
                accountEntity.setAddressLength(1);
                mRepo.insertAccount(accountEntity);
                mRepo.insertAddress(addressEntity);
            } else {
                AccountEntity accountEntity = accounts.get(0);
                JSONObject object = new JSONObject();
                object.put("public_key", publicKey);
                accountEntity.setAddition(object.toString());
                AddressEntity addressEntity = getArweaveAddress(publicKey);
                coin.setAddressCount(1);
                accountEntity.setAddressLength(1);
                mRepo.updateAccount(accountEntity);
                mRepo.insertAddress(addressEntity);
            }
        } catch (Exception e) {
            Log.e(TAG, "addArweaveAddressToDB: ", e);
        } finally {
            generatingAddress.postValue(false);
            clearToken();
        }
    }

    public static String formatHex(byte[] data) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private AddressEntity getArweaveAddress(String publicKey) {
        String address = formatHex(Util.sha256(Hex.decode(publicKey)));
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCoinId(Coins.AR.coinId());
        addressEntity.setAddressString(address);
        addressEntity.setName("AR Account");
        addressEntity.setBelongTo(Utilities.getCurrentBelongTo(mContext));
        addressEntity.setIndex(0);
        addressEntity.setDisplayName("AR Account");
        return addressEntity;
    }

    public MutableLiveData<UR> generateSyncData() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                AccountEntity account = mRepo.loadArweaveAccount();
                JSONObject json = new JSONObject(account.getAddition());
                String public_key = json.getString("public_key");
                byte[] publicKey = Hex.decode(public_key);
                byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
                String device = "Keystone";
                ArweaveCryptoAccount arweaveCryptoAccount = new ArweaveCryptoAccount(masterFingerprint, publicKey, device);
                UR ur = arweaveCryptoAccount.toUR();
                data.postValue(ur);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return data;
    }
}
