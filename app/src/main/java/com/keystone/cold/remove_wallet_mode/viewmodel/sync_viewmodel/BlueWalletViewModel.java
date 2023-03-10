package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.B58;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoAccount;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.CryptoOutput;
import com.sparrowwallet.hummingbird.registry.PathComponent;
import com.sparrowwallet.hummingbird.registry.ScriptExpression;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlueWalletViewModel extends AndroidViewModel {
    private static int PURPOSE_LEGACY = 44;
    private static int PURPOSE_NATIVE_SEGWIT = 84;
    private static int PURPOSE_NESTED_SEGWIT = 49;

    public BlueWalletViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UR> generateSyncUR() {
        MutableLiveData<UR> data = new MutableLiveData<>();
        AppExecutors.getInstance().networkIO().execute(() -> {
            UR ur = generateCryptoAccount().toUR();
            data.postValue(ur);
        });
        return data;
    }

    public CryptoAccount generateCryptoAccount() {
        String mfp = new GetMasterFingerprintCallable().call();
        List<CryptoOutput> outputs = new ArrayList<>();
        outputs.add(generateNativeSegwitOutput(mfp));
        outputs.add(generateLegacyOutput(mfp));
        outputs.add(generateNestedSegwitOutput(mfp));
        return new CryptoAccount(Hex.decode(mfp), outputs);
    }

    private CryptoHDKey generateCryptoHDKey(String masterKeyFingerprint, String xpub, int purpose) {
        byte[] xpubBytes = new B58().decodeAndCheck(xpub);
        byte[] parentFp = Arrays.copyOfRange(xpubBytes, 5, 9);
        byte[] key = Arrays.copyOfRange(xpubBytes, 45, 78);
        byte[] chainCode = Arrays.copyOfRange(xpubBytes, 13, 45);
        int depth = xpubBytes[4];
        CryptoCoinInfo coinInfo = new CryptoCoinInfo(1, 0);
        CryptoKeypath origin = new CryptoKeypath(Arrays.asList(
                new PathComponent(purpose, true),
                new PathComponent(0, true),
                new PathComponent(0, true)), Hex.decode(masterKeyFingerprint), depth);

        CryptoKeypath children = new CryptoKeypath(Arrays.asList(
                new PathComponent(0, false),
                new PathComponent(false)), null);

        return new CryptoHDKey(false, key, chainCode, coinInfo, origin, children, parentFp);
    }

    private CryptoOutput generateLegacyOutput(String masterKeyFingerprint) {
        List<ScriptExpression> scriptExpressions = new ArrayList<>();
        scriptExpressions.add(ScriptExpression.PUBLIC_KEY_HASH);
        String xpub = new GetExtendedPublicKeyCallable(BitcoinTxViewModel.BTCLegacyPath).call();
        return new CryptoOutput(scriptExpressions, generateCryptoHDKey(masterKeyFingerprint, xpub, PURPOSE_LEGACY));
    }

    private CryptoOutput generateNestedSegwitOutput(String masterKeyFingerprint) {
        List<ScriptExpression> scriptExpressions = new ArrayList<>();
        scriptExpressions.add(ScriptExpression.SCRIPT_HASH);
        scriptExpressions.add(ScriptExpression.WITNESS_PUBLIC_KEY_HASH);
        String xpub = new GetExtendedPublicKeyCallable(BitcoinTxViewModel.BTCNestedSegwitPath).call();
        return new CryptoOutput(scriptExpressions, generateCryptoHDKey(masterKeyFingerprint, xpub, PURPOSE_NESTED_SEGWIT));
    }

    private CryptoOutput generateNativeSegwitOutput(String masterKeyFingerprint) {
        List<ScriptExpression> scriptExpressions = new ArrayList<>();
        scriptExpressions.add(ScriptExpression.WITNESS_PUBLIC_KEY_HASH);
        String xpub = new GetExtendedPublicKeyCallable(BitcoinTxViewModel.BTCNativeSegwitPath).call();
        return new CryptoOutput(scriptExpressions, generateCryptoHDKey(masterKeyFingerprint, xpub, PURPOSE_NATIVE_SEGWIT));
    }
}
