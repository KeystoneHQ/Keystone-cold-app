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
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.MnemonicUtils;
import com.keystone.coinlib.utils.Bip39;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.BuildConfig;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.callables.GetRandomEntropyCallable;
import com.keystone.cold.callables.GetVaultIdCallable;
import com.keystone.cold.callables.RestartSeCallable;
import com.keystone.cold.callables.UpdatePassphraseCallable;
import com.keystone.cold.callables.VerifyMnemonicCallable;
import com.keystone.cold.callables.WebAuthCallableUpgrade;
import com.keystone.cold.callables.WriteMnemonicCallable;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.util.HashUtil;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import iton.slip.secret.Group;
import iton.slip.secret.Share;
import iton.slip.secret.SharedSecret;
import iton.slip.secret.SharedSecretException;
import iton.slip.secret.words.Mnemonic;

import static com.keystone.cold.mnemonic.MnemonicInputTable.THIRTYTHREE;
import static com.keystone.cold.mnemonic.MnemonicInputTable.TWEENTY;

public class SetupVaultViewModel extends AndroidViewModel {

    public static final int VAULT_STATE_NOT_CREATE = 0;
    public static final int VAULT_STATE_CREATING = 1;
    public static final int VAULT_STATE_CREATED = 2;
    public static final int VAULT_STATE_CREATING_FAILED = 3;

    private final ObservableField<String> pwd1 = new ObservableField<>("");
    private final ObservableField<String> pwd2 = new ObservableField<>("");
    private final MutableLiveData<String> webAuthCode = new MutableLiveData<>(null);
    private final ObservableField<Integer> mnemonicCount = new ObservableField<>(24);
    private final MutableLiveData<Integer> vaultCreateState = new MutableLiveData<>(VAULT_STATE_NOT_CREATE);
    private final MutableLiveData<String> mnemonic = new MutableLiveData<>("");
    private List<String> shares;
    private int sequence = 0;
    public Share firstShare;
    private boolean isShardingMnemonic;
    private boolean isCreateMnemonic;
    private String vaultId;

    private final DataRepository mRepository;
    private String password;
    private String signature;

    public SetupVaultViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    public void calcAuthCode(String data) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Bundle bundle = getApplication().getContentResolver().call(
                    Uri.parse("content://settings"), "web_auth_priv_key", null, null);
            String rsaPrivKey = bundle == null? "null" :  bundle.getString("key");
            String authCode = new WebAuthCallableUpgrade(Base64.decode(data),
                    rsaPrivKey,
                    Hex.decode(BuildConfig.WEB_AUTH_R1_PUBLIC_KEY)).call();
            webAuthCode.postValue(format(authCode));
        });
    }

    private String format(String replace) {
        if (TextUtils.isEmpty(replace)) {
            return "";
        }
        String regex = "(.{4})";
        replace = replace.replaceAll(regex, "$1 ");
        return replace.trim();
    }

    public MutableLiveData<String> getWebAuthCode() {
        return webAuthCode;
    }

    public MutableLiveData<Integer> getVaultCreateState() {
        return vaultCreateState;
    }

    public ObservableField<String> getPwd1() {
        return pwd1;
    }

    public ObservableField<String> getPwd2() {
        return pwd2;
    }

    @NonNull
    public ObservableField<Integer> getMnemonicCount() {
        return mnemonicCount;
    }

    public void setMnemonicCount(int mnemonicCount) {
        isShardingMnemonic = mnemonicCount == TWEENTY || mnemonicCount == THIRTYTHREE;
        this.mnemonicCount.set(mnemonicCount);
    }

    public void setPassword(String password) {
        if (TextUtils.isEmpty(password)) return;
        this.password = password;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean validateMnemonic(String mnemonic) {
        if (isShardingMnemonic) {
            try {
                Mnemonic.INSTANCE.decode(mnemonic);
            } catch (SharedSecretException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else if (isCreateMnemonic) {
            return true;
        } else {
            return Bip39.validateMnemonic(mnemonic);
        }
    }

    public void writeMnemonic(String mnemonic) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            vaultCreateState.postValue(VAULT_STATE_CREATING);
            if (new WriteMnemonicCallable(mnemonic, password).call()) {
                vaultId = new GetVaultIdCallable().call();
                mRepository.clearDb();
                vaultCreateState.postValue(VAULT_STATE_CREATED);
                password = null;
            } else {
                vaultCreateState.postValue(VAULT_STATE_CREATING_FAILED);
            }
        });
    }

    public void writeShardingMasterSeed() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                byte[] masterSeed = new SharedSecret().combineWithoutDecrypt(shares.toArray(new String[0]));
                vaultCreateState.postValue(VAULT_STATE_CREATING);
                if(new WriteMnemonicCallable(masterSeed,
                        firstShare.id, firstShare.iteration_exponent, password).call()) {
                    vaultId = new GetVaultIdCallable().call();
                    mRepository.clearDb();
                    vaultCreateState.postValue(VAULT_STATE_CREATED);
                    password = null;
                } else {
                    vaultCreateState.postValue(VAULT_STATE_CREATING_FAILED);
                }
            } catch (SharedSecretException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                resetSharding();
            }
        });
    }

    public byte[] verifyShardingMnemonic() {
        try {
            byte[] masterSeed = new SharedSecret().combineWithoutDecrypt(shares.toArray(new String[0]));

            if (new VerifyMnemonicCallable(null, masterSeed, firstShare.id).call()) {
                return masterSeed;
            }
        } catch (SharedSecretException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePassphrase(String passphrase) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            vaultCreateState.postValue(VAULT_STATE_CREATING);
            boolean success;
            if (TextUtils.isEmpty(passphrase)) {
                success = new RestartSeCallable().call();
            } else {
                success = new UpdatePassphraseCallable(passphrase, password, signature).call();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (success) {
                vaultId = new GetVaultIdCallable().call();
                deleteHiddenVaultData();
                password = null;
                signature = null;
                vaultCreateState.postValue(VAULT_STATE_CREATED);
            } else {
                vaultCreateState.postValue(VAULT_STATE_CREATING_FAILED);
            }

        });
    }

    public String getVaultId() {
        return vaultId;
    }

    public PasswordValidationResult validatePassword() {
        if (Objects.requireNonNull(pwd1.get()).length() < 10) {
            return PasswordValidationResult.RESULT_TOO_SHORT;
        } else if (!validInput(Objects.requireNonNull(pwd1.get()))) {
            return PasswordValidationResult.RESULT_INPUT_WRONG;
        } else {
            return PasswordValidationResult.RESULT_OK;
        }
    }

    private boolean validInput(String s) {
        char[] chars = s.toCharArray();
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        for (char c : chars) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
                continue;
            }
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
        }
        return hasDigit && hasLowerCase && hasUpperCase;
    }

    public void generateSlip39Mnemonic(int threshold, int total) {
        String entropy = new GetRandomEntropyCallable(128).call();
        SharedSecret sharedSecret = new SharedSecret();
        try {
            shares = sharedSecret.generateWithoutEncrypt(Hex.decode(entropy), (byte) 1,
                    Collections.singletonList(new Group(threshold, total)));
            firstShare = Mnemonic.INSTANCE.decode(shares.get(0));
            sequence = 0;
            if (!MnemonicUtils.isValidateEntropy(Hex.decode(entropy))) {
                this.mnemonic.postValue("");
            } else {
                this.mnemonic.postValue(Bip39.generateMnemonic(entropy));
            }
            isShardingMnemonic = true;
            mnemonicCount.set(TWEENTY);
        } catch (SharedSecretException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void nextSequence() {
        sequence++;
    }

    public int currentSequence() {
        return sequence;
    }

    public int totalShares() {
        return shares.size();
    }

    public void resetSharding() {
        sequence = 0;
        isShardingMnemonic = false;
        if (shares!= null) {
            shares.clear();
        }
        firstShare = null;
    }

    public boolean isShardingMnemonic() {
        return isShardingMnemonic;
    }

    public String getShareByIndex(int i) {
        return shares.get(i);
    }

    public List<String> getShares() {
        return shares;
    }

    public void generateRandomMnemonic() {
        Executor executor = Executors.newSingleThreadExecutor();
        Runnable task = () -> {
            String entropy = new GetRandomEntropyCallable().call();
            if (!MnemonicUtils.isValidateEntropy(Hex.decode(entropy))) {
                this.mnemonic.postValue("");
            } else {
                this.mnemonic.postValue(Bip39.generateMnemonic(entropy));
            }
        };
        executor.execute(task);
    }

    public void generateMnemonicFromDiceRolls(byte[] diceRolls) {
        //Use the same algorithm as https://iancoleman.io/bip39/
        StringBuilder rolls = new StringBuilder();
        for (byte b: diceRolls) {
            rolls.append(b % 6);
        }
        String entropy = Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(rolls.toString())));
        String mnemonic = Bip39.generateMnemonic(entropy);
        this.mnemonic.postValue(mnemonic);
    }

    public LiveData<String> getMnemonic() {
        return mnemonic;
    }

    public void presetData(List<CoinEntity> coins, final Runnable onComplete) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            for (CoinEntity coin : coins) {
                CoinEntity coinEntity = mRepository.loadCoinSync(coin.getCoinId());
                if (coinEntity != null) {
                    List<AccountEntity> accountEntities = mRepository.loadAccountsForCoin(coinEntity);
                    if (accountEntities.size() != 0) {
                        continue;
                    }
                }
                String xPub = new GetExtendedPublicKeyCallable(coin.getAccounts().get(0).getHdPath()).call();
                coin.setExPub(xPub);
                if (coinEntity == null) {
                    long id = mRepository.insertCoin(coin);
                    coin.setId(id);
                } else {
                    coin.setId(coinEntity.getId());
                }
                boolean isFirstAccount = true;
                for (AccountEntity account : coin.getAccounts()) {
                    if (!isFirstAccount) {
                        xPub = new GetExtendedPublicKeyCallable(account.getHdPath()).call();
                    }
                    isFirstAccount = false;
                    if (TextUtils.isEmpty(xPub)) {
                        continue;
                    }
                    account.setCoinId(coin.getId());
                    account.setExPub(xPub);
                    mRepository.insertAccount(account);
                    if (!Coins.showPublicKey(coin.getCoinCode())) {
                        new AddAddressViewModel.AddAddressTask(coin, mRepository, null)
                                .execute(coin.getDisplayName() + "-0");
                    }
                }
            }
            if (onComplete != null) {
                AppExecutors.getInstance().mainThread().execute(onComplete);
            }
        });
    }

    private void deleteHiddenVaultData() {
        mRepository.deleteHiddenVaultData();
    }

    public LiveData<List<CoinEntity>> getCoins() {
        return mRepository.reloadCoins();
    }

    public AddShareResult addShare(String share) throws SharedSecretException {
        if (shares == null || shares.isEmpty()) {
            shares = new ArrayList<>();
            shares.add(share);
            firstShare = Mnemonic.INSTANCE.decode(shares.get(0));
        } else {
            if (shares.contains(share)) {
                return AddShareResult.RESULT_REPEAT;
            } else {
                Share currentShare = Mnemonic.INSTANCE.decode(share);
                if (firstShare.id != currentShare.id) {
                    return AddShareResult.RESULT_NOT_MATCH;
                } else {
                    shares.add(share);
                }
            }
        }
        try {
            new SharedSecret().combineWithoutDecrypt(shares.toArray(new String[0]));
        } catch (SharedSecretException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return AddShareResult.RESULT_NEED_MORE;
        }

        return AddShareResult.RESULT_OK;
    }

    public boolean isCreateMnemonic() {
        return isCreateMnemonic;
    }

    public void completeMnemonic(String mnemonic) {
        this.mnemonic.postValue(mnemonic);
    }

    public void setIsCreateMnemonic(boolean b) {
        isCreateMnemonic = b;
    }

    public enum PasswordValidationResult {
        RESULT_OK,
        RESULT_NOT_MATCH,
        RESULT_TOO_SHORT,
        RESULT_INPUT_WRONG,
    }

    public enum AddShareResult {
        RESULT_OK,
        RESULT_NEED_MORE,
        RESULT_NOT_MATCH,
        RESULT_REPEAT,
    }
}
