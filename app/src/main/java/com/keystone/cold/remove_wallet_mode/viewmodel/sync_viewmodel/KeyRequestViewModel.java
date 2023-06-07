package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import static com.keystone.cold.util.URRegistryHelper.KEY_NAME;
import static com.keystone.cold.util.URRegistryHelper.getPathComponents;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.protocol.EncodeConfig;
import com.keystone.cold.protocol.builder.SyncBuilder;
import com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.KeyRequestApproveFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.ADASetupManager;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.keystone.cold.util.URRegistryHelper;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.CryptoMultiAccounts;
import com.sparrowwallet.hummingbird.registry.RegistryType;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;

public class KeyRequestViewModel extends AndroidViewModel {
    private final DataRepository mRepository;

    public KeyRequestViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    public static boolean validateRequest(List<KeyRequestApproveFragment.Schema> schemas) {
        for (KeyRequestApproveFragment.Schema schema : schemas) {
            if (schema.getCurve() == 0 && schema.getAlgo() == 1) {
                // secp256k1 + bip32ed25519 is not a valid schema
                return false;
            }
            String path = schema.getPath();
            String[] pieces = path.split("/");
            if (pieces.length < 4) {
                // m + purpose + cointype + account
                // only give away keys under account level, or it may be dangerous for user's privacy.
                return false;
            }
        }
        return true;
    }

    public MutableLiveData<Boolean> checkNeedPassword(List<KeyRequestApproveFragment.Schema> schemas) {
        MutableLiveData<Boolean> result = new MutableLiveData<>(null);
        AppExecutors.getInstance().diskIO().execute(() -> {
            for (KeyRequestApproveFragment.Schema schema : schemas) {
                //bip32-ed25519, we only stored ADA keys. m/1852'/1815'/[0-23]'
                if (schema.getAlgo() == 1) {
                    if (!schema.getPath().toLowerCase().startsWith(ADASetupManager.ADARootPath)) {
                        result.postValue(true);
                        break;
                    } else {
                        // String path = schema.getPath();
                        // String[] pieces = path.split("/");
                        // TODO: check path
                    }
                }
            }
            result.postValue(false);
        });
        return result;
    }

    public LiveData<UR> generateSyncUR(List<KeyRequestApproveFragment.Schema> schemas) {
        MutableLiveData<UR> sync = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CryptoHDKey> cryptoHDKeyList = new ArrayList<>();
            byte[] masterFingerprint = Hex.decode(new GetMasterFingerprintCallable().call());
            for (KeyRequestApproveFragment.Schema schema : schemas) {
                if (schema.getCurve() == 0) {
                    // secp256k1 + slip10
                    String xpub = new GetExtendedPublicKeyCallable(schema.getPath(), Coins.CURVE.SECP256K1).call();
                    ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
                    byte[] key = extendedPublicKey.getKey();
                    byte[] chainCode = extendedPublicKey.getChainCode();
                    CryptoKeypath origin = new CryptoKeypath(getPathComponents(schema.getPath()), masterFingerprint, (int) extendedPublicKey.getDepth());
                    CryptoHDKey hdkey = new CryptoHDKey(false, key, chainCode, null, origin, null, null, null, null);
                    cryptoHDKeyList.add(hdkey);
                } else if (schema.getCurve() == 1 && schema.getAlgo() == 0) {
                    // ed25519 + slip10
                    String xpub = new GetExtendedPublicKeyCallable(schema.getPath(), Coins.CURVE.ED25519).call();
                    ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
                    byte[] key = extendedPublicKey.getKey();
                    CryptoKeypath origin = new CryptoKeypath(getPathComponents(schema.getPath()), masterFingerprint, (int) extendedPublicKey.getDepth());
                    CryptoHDKey hdkey = new CryptoHDKey(false, key, null, null, origin, null, null, null, null);
                    cryptoHDKeyList.add(hdkey);
                } else {
                    // ed25519 + bip32-ed25519
                    CoinEntity ada = mRepository.loadCoinSync(Coins.ADA.coinId());
                    List<AccountEntity> accounts = mRepository.loadAccountsForCoin(ada);
                    Optional<AccountEntity> target = accounts.stream().filter(accountEntity -> accountEntity.getHdPath().equalsIgnoreCase(schema.getPath())).findAny();
                    target.ifPresent(accountEntity -> {
                        String xpub = accountEntity.getExPub();
                        byte[] extended_key = Hex.decode(xpub);
                        byte[] key = Arrays.copyOfRange(extended_key, 0, 32);
                        byte[] chainCode = Arrays.copyOfRange(extended_key, 32, 64);
                        CryptoKeypath origin = new CryptoKeypath(getPathComponents(schema.getPath()), masterFingerprint);
                        CryptoHDKey hdkey = new CryptoHDKey(false, key, chainCode, null, origin, null, null, null, null);
                        cryptoHDKeyList.add(hdkey);
                    });
                    // don't derive keys currently.
                }
            }
            CryptoMultiAccounts accounts = new CryptoMultiAccounts(masterFingerprint, cryptoHDKeyList, KEY_NAME);
            sync.postValue(accounts.toUR());
        });
        return sync;
    }
}
