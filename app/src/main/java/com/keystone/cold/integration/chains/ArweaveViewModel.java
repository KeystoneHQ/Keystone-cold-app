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
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.cryptocore.RCCService;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.encryption.EncryptionCoreProvider;
import com.keystone.cold.ui.views.AuthenticateModal;

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
            Log.d(TAG, "getRSAPublicKey: " + portName);
            String result = RCCService.getRSAPublicKey(new RCCService.Passport(token.password, isMainWallet, portName));
            Log.d(TAG, "getRSAPublicKey: " + result);
            addArweaveAddressToDB(result);
        });
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
        }
    }

    private AddressEntity getArweaveAddress(String publicKey) {
        String address = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(Util.sha256(Hex.decode(publicKey)));
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCoinId(Coins.AR.coinId());
        Log.d(TAG, "getArweaveAddress: " + address);
        addressEntity.setAddressString(address);
        addressEntity.setName("AR Account");
        addressEntity.setBelongTo(Utilities.getCurrentBelongTo(mContext));
        addressEntity.setIndex(0);
        addressEntity.setDisplayName("AR Account");
        return addressEntity;
    }
}
