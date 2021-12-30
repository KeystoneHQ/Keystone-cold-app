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

package com.keystone.cold.viewmodel.tx;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

abstract class Base extends AndroidViewModel {

    public static final String STATE_NONE = "";
    public static final String STATE_SIGNING = "signing";
    public static final String STATE_SIGN_FAIL = "signing_fail";
    public static final String STATE_SIGN_SUCCESS = "signing_success";
    public static final String TAG = "Vault.TxConfirm";
    protected final DataRepository mRepository;
    protected final MutableLiveData<Boolean> addingAddress = new MutableLiveData<>();
    protected final MutableLiveData<TxEntity> observableTx = new MutableLiveData<>();
    protected final MutableLiveData<Exception> parseTxException = new MutableLiveData<>();
    protected String coinCode;
    protected final MutableLiveData<String> signState = new MutableLiveData<>();
    protected AuthenticateModal.OnVerify.VerifyToken token;
    protected WatchWallet watchWallet;

    Base(@NonNull Application application) {
        super(application);
        watchWallet = WatchWallet.getWatchWallet(application);
        observableTx.setValue(null);
        mRepository = MainApplication.getApplication().getRepository();
    }

    public MutableLiveData<TxEntity> getObservableTx() {
        return observableTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    public MutableLiveData<String> getSignState() {
        return signState;
    }

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    protected SignCallback initSignTxCallback() {
        return new SignCallback() {
            @Override
            public void startSign() {
                signState.postValue(STATE_SIGNING);
            }

            @Override
            public void onFail() {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
            }

            @Override
            public void onSuccess(String txId, String rawTx) {
                signState.postValue(STATE_SIGN_SUCCESS);
                TxEntity tx = onSignSuccess(txId, rawTx);
//                if (Coins.showPublicKey(tx.getCoinCode())) {
//                    persistAddress(tx.getCoinCode(), tx.getCoinId(), tx.getFrom());
//                }
                new ClearTokenCallable().call();
            }

            @Override
            public void postProgress(int progress) {

            }
        };
    }


    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        tx.setSignedHex(rawTx);
        mRepository.insertTx(tx);
        return tx;
    }

    protected void addAddress(int addressIndex) {
        CoinEntity coin = mRepository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode));
        int addressLength = mRepository.loadAccountsForCoin(coin).get(0).getAddressLength();

        if (addressLength < addressIndex + 1) {
            String[] names = new String[addressIndex + 1 - addressLength];
            int index = 0;
            for (int i = addressLength; i < addressIndex + 1; i++) {
                names[index++] = coinCode + "-" + i;
            }
            final CountDownLatch mLatch = new CountDownLatch(1);
            addingAddress.postValue(true);
            new AddAddressViewModel.AddAddressTask(coin, mRepository, mLatch::countDown)
                    .execute(names);
            try {
                mLatch.await();
                addingAddress.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getAuthToken() {
        String authToken = null;
        if (!TextUtils.isEmpty(token.password)) {
            authToken = new GetPasswordTokenCallable(token.password).call();
        } else if (token.signature != null) {
            String message = new GetMessageCallable().call();
            if (!TextUtils.isEmpty(message)) {
                try {
                    token.signature.update(Hex.decode(message));
                    byte[] signature = token.signature.sign();
                    byte[] rs = Util.decodeRSFromDER(signature);
                    if (rs != null) {
                        authToken = new VerifyFingerprintCallable(Hex.toHexString(rs)).call();
                    }
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
            }
        }
        AuthenticateModal.OnVerify.VerifyToken.invalid(token);
        return authToken;
    }

    public String getTxId() {
        return Objects.requireNonNull(observableTx.getValue()).getTxId();
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableTx.getValue()).getSignedHex();
    }

    protected long getUniversalSignIndex(Context context) {
        long current = Utilities.getPrefs(context).getLong("universal_sign_index", 0);
        Utilities.getPrefs(context).edit().putLong("universal_sign_index", current + 1).apply();
        return current;
    }
}
