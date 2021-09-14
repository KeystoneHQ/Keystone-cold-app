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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.BTC.UtxoTx;
import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.XRP.Xrp;
import com.keystone.coinlib.coins.XRP.XrpImpl;
import com.keystone.coinlib.coins.polkadot.DOT.Dot;
import com.keystone.coinlib.coins.polkadot.DOT.DotImpl;
import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.AddressIndex;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.DUPLICATE_TX;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;

public class KeystoneTxViewModel extends Base {

    private final MutableLiveData<Integer> feeAttachCheckingResult = new MutableLiveData<>();
    private AbsTx transaction;
    protected String coinCode;
    protected final MutableLiveData<String> signState = new MutableLiveData<>();
    protected AuthenticateModal.OnVerify.VerifyToken token;
    protected WatchWallet watchWallet;

    public KeystoneTxViewModel(@NonNull Application application) {
        super(application);
        watchWallet = WatchWallet.getWatchWallet(application);
        observableTx.setValue(null);
    }

    public MutableLiveData<TxEntity> getObservableTx() {
        return observableTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public void parseTxData(String json) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                JSONObject object = new JSONObject(json);
                Log.i(TAG, "object = " + object.toString(4));
                transaction = AbsTx.newInstance(object);
                if (transaction == null) {
                    observableTx.postValue(null);
                    parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
                    return;
                }
                TxEntity tx = generateTxEntity(object);
                observableTx.postValue(tx);
                if (Coins.BTC.coinCode().equals(transaction.getCoinCode())) {
                    feeAttackChecking(tx);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void feeAttackChecking(TxEntity txEntity) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String inputs = txEntity.getFrom();
            String outputs = txEntity.getTo();
            List<TxEntity> txs = mRepository.loadAllTxSync(Coins.BTC.coinId());
            for (TxEntity tx : txs) {
                if (inputs.equals(tx.getFrom()) && outputs.equals(tx.getTo())) {
                    feeAttachCheckingResult.postValue(DUPLICATE_TX);
                    break;
                } else if (outputs.equals(tx.getTo())) {
                    if (txEntity.getAmount().endsWith("USDT")) {
                        if (txEntity.getAmount().equals(tx.getAmount())) {
                            feeAttachCheckingResult.postValue(SAME_OUTPUTS);
                        } else {
                            feeAttachCheckingResult.postValue(NORMAL);
                        }
                    } else {
                        feeAttachCheckingResult.postValue(SAME_OUTPUTS);
                    }
                    break;
                } else {
                    feeAttachCheckingResult.postValue(NORMAL);
                }
            }
        });
    }

    public LiveData<Integer> feeAttackChecking() {
        return feeAttachCheckingResult;
    }

    private TxEntity generateTxEntity(JSONObject object) throws JSONException {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(20);
        coinCode = Objects.requireNonNull(transaction).getCoinCode();
        tx.setSignId(object.getString("signId"));
        tx.setTimeStamp(object.optLong("timestamp"));
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setFrom(getFromAddress());
        tx.setTo(getToAddress());
        tx.setAmount(nf.format(transaction.getAmount()) + " " + transaction.getUnit());
        tx.setFee(nf.format(transaction.getFee()) + " " + tx.getDisplayName());
        tx.setMemo(transaction.getMemo());
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private String getToAddress() {
        String to = transaction.getTo();

        if (transaction instanceof UtxoTx) {
            JSONArray outputs = ((UtxoTx) transaction).getOutputs();
            if (outputs != null) {
                return outputs.toString();
            }
        }

        return to;
    }

    private String getFromAddress() {
        if (!TextUtils.isEmpty(transaction.getFrom())) {
            return transaction.getFrom();
        } else if (Coins.isPolkadotFamily(coinCode)) {
            AddressEntity addressEntity = mRepository.loadAddressBypath(transaction.getHdPath());
            return addressEntity.getAddressString();
        }
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String[] externalPath = Stream.of(paths)
                .filter(this::isExternalPath)
                .toArray(String[]::new);
        ensureAddressExist(externalPath);

        return Stream.of(externalPath)
                .distinct()
                .map(path -> mRepository.loadAddressBypath(path).getAddressString())
                .reduce((s1, s2) -> s1 + AbsTx.SEPARATOR + s2)
                .orElse("");
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;

    }

    private boolean isExternalPath(@NonNull String path) {
        try {
            return CoinPath.parsePath(path).getParent().isExternal();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void ensureAddressExist(String[] paths) {
        if (paths == null || paths.length == 0) {
            return;
        }
        String maxIndexHdPath = paths[0];
        if (paths.length > 1) {
            int max = getAddressIndex(paths[0]);
            for (String path : paths) {
                if (getAddressIndex(path) > max) {
                    max = getAddressIndex(path);
                    maxIndexHdPath = path;
                }
            }
        }
        AddressEntity address = mRepository.loadAddressBypath(maxIndexHdPath);
        if (address == null) {
            addAddress(getAddressIndex(maxIndexHdPath));
        }
    }

    public LiveData<Boolean> getAddingAddressState() {
        return addingAddress;
    }

    private int getAddressIndex(String hdPath) {
        try {
            return CoinPath.parsePath(hdPath).getValue();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MutableLiveData<String> getSignState() {
        return signState;
    }

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            Signer[] signer = initSigners();
            signTransaction(callback, signer);
        });

    }

//    private void persistAddress(String coinCode, String coinId, String address) {
//        String path;
//        switch (coinCode) {
//            case "EOS":
//                path = "M/44'/194'/0'/0/0";
//                break;
//            case "IOST":
//                path = "M/44'/291'/0'/0'/0'";
//                break;
//            default:
//                return;
//        }
//        AddressEntity addressEntity = new AddressEntity();
//        addressEntity.setPath(path);
//        addressEntity.setAddressString(address);
//        addressEntity.setCoinId(coinId);
//        addressEntity.setIndex(0);
//        addressEntity.setName(coinCode + "-0");
//        addressEntity.setBelongTo(Utilities.getCurrentBelongTo(getApplication()));
//        mRepository.insertAddress(addressEntity);
//    }

    void signTransaction(@NonNull SignCallback callback, Signer... signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        switch (transaction.getTxType()) {
            case "DOT":
//            case "KSM":
                Dot dot = new Dot(new DotImpl(coinCode));
                dot.generateTransaction(transaction, callback, signer);
                break;
            case "ETH":
                Eth eth = new Eth(new EthImpl(Eth.CHAIN_ID));
                eth.generateTransaction(transaction, callback, signer);
                break;
//            case "ETC":
//                Etc etc = new Etc(new EthImpl(Etc.CHAIN_ID));
//                etc.generateTransaction(transaction, callback, signer);
//                break;
            case "XRP":
                Xrp xrp = new Xrp(new XrpImpl());
                xrp.generateTransaction(transaction, callback, signer);
                break;
            default:
                AbsCoin coin = AbsCoin.newInstance(coinCode);
                Objects.requireNonNull(coin).generateTransaction(transaction, callback, signer);
        }
    }

    private Signer[] initSigners() {
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String coinCode = transaction.getCoinCode();
        String[] distinctPaths = Stream.of(paths).distinct().toArray(String[]::new);
        Signer[] signer = new Signer[distinctPaths.length];
        boolean shouldProvidePublicKey = Signer.shouldProvidePublicKey(transaction.getCoinCode());
        String exPub = null;
        if (shouldProvidePublicKey) {
            exPub = mRepository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode)).getExPub();
        }

        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }

        for (int i = 0; i < distinctPaths.length; i++) {
            if (shouldProvidePublicKey) {
                String pubKey;
                if (Coins.curveFromCoinCode(coinCode) == Coins.CURVE.ED25519 || Coins.isPolkadotFamily(coinCode)) {
                    byte[] bytes = new B58().decode(exPub);
                    byte[] pubKeyBytes = Arrays.copyOfRange(bytes, bytes.length - 4 - 32, bytes.length - 4);
                    pubKey = Hex.toHexString(pubKeyBytes);
                } else {
                    pubKey = Util.getPublicKeyHex(exPub, distinctPaths[i]);
                }
                signer[i] = new ChipSigner(distinctPaths[i].toLowerCase(), authToken, pubKey);
            } else {
                signer[i] = new ChipSigner(distinctPaths[i].toLowerCase(), authToken);
            }
        }
        return signer;
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

    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public boolean isAddressInWhiteList(String address) {
        try {
            return sExecutor.submit(() -> mRepository.queryWhiteList(address) != null).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected long getUniversalSignIndex(Context context) {
        long current = Utilities.getPrefs(context).getLong("universal_sign_index", 0);
        Utilities.getPrefs(context).edit().putLong("universal_sign_index", current + 1).apply();
        return current;
    }
}
