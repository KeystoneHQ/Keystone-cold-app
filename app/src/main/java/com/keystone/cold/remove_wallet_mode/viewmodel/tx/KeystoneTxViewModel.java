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

package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.DUPLICATE_TX;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
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
import com.keystone.coinlib.coins.TRON.Tron;
import com.keystone.coinlib.coins.TRON.TronImpl;
import com.keystone.coinlib.coins.XRP.Xrp;
import com.keystone.coinlib.coins.XRP.XrpImpl;
import com.keystone.coinlib.coins.polkadot.DOT.Dot;
import com.keystone.coinlib.coins.polkadot.DOT.DotImpl;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.path.AddressIndex;
import com.keystone.coinlib.path.CoinPath;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMessageCallable;
import com.keystone.cold.callables.GetPasswordTokenCallable;
import com.keystone.cold.callables.VerifyFingerprintCallable;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidChangeAddressException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.AddAddressViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class KeystoneTxViewModel extends BaseTxViewModel<TxEntity> {
    private static final String TAG = "KeystoneTxViewModel";
    private static final String BTC_SEGWIT_PATH = "M/49'/0'/0'/";
    private static final String BTC_LEGACY_PATH = "M/44'/0'/0'/";
    private static final String BTC_NATIVE_SEGWIT_PATH = "M/84'/0'/0'/";
    private static final String BTC_TESTNET_SEGWIT_PATH = "M/49'/1'/0'/";
    private static final String BTC_TESTNET_LEGACY_PATH = "M/44'/1'/0'/";
    private static final String BTC_TESTNET_NATIVE_SEGWIT_PATH = "M/84'/1'/0'/";

    public KeystoneTxViewModel(@NonNull Application application) {
        super(application);
        observableTransaction.setValue(null);
        BTCXPubMap = new HashMap<>();
    }

    private final MutableLiveData<Integer> feeAttachCheckingResult = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> addingAddress = new MutableLiveData<>();
    private AbsTx transaction;
    protected HashMap<String, BTCDeriver> BTCXPubMap;

    public String getCoinCode() {
        return coinCode;
    }

    private String coinCode;

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String json = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                JSONObject object = new JSONObject(json);
                Log.i(TAG, "object = " + object.toString(4));
                transaction = AbsTx.newInstance(object);
                if (transaction == null) {
                    observableException.postValue(new InvalidTransactionException(MainApplication.getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                    return;
                }
                TxEntity tx = generateTxEntity(object);
                if (Coins.isBTCFamily(transaction.getCoinCode())) {
                    feeAttackChecking(tx);
                    if (!checkBTCChangeAddress((UtxoTx) transaction)) {
                        observableException.postValue(InvalidChangeAddressException.newInstance());
                        return;
                    }
                } else {
                    if (transaction instanceof UtxoTx) {
                        if (!checkChangeAddress(transaction)) {
                            observableException.postValue(InvalidChangeAddressException.newInstance());
                            return;
                        }
                    }
                }
                observableTransaction.postValue(tx);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void parseExistingTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            observableTransaction.postValue(txEntity);
        });
    }

    private boolean checkChangeAddress(AbsTx utxoTx) {
        UtxoTx.ChangeAddressInfo changeAddressInfo = ((UtxoTx) utxoTx).getChangeAddressInfo();
        if (changeAddressInfo == null) {
            return true;
        }
        String hdPath = changeAddressInfo.hdPath;
        String address = changeAddressInfo.address;
        String exPub = repository.loadCoinEntityByCoinCode(utxoTx.getCoinCode()).getExPub();
        AbsDeriver deriver = AbsDeriver.newInstance(utxoTx.getCoinCode());

        try {
            AddressIndex addressIndex = CoinPath.parsePath(hdPath);
            int change = addressIndex.getParent().getValue();
            int index = addressIndex.getValue();
            String expectAddress = Objects.requireNonNull(deriver).derive(exPub, change, index);
            return address.equals(expectAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkBTCChangeAddress(UtxoTx tx) {
        boolean flag = true;
        for (UtxoTx.ChangeAddressInfo changeAddress :
                tx.getChangeAddressInfoList()) {
            String hdPath = changeAddress.hdPath;
            String address = changeAddress.address;
            for (String key :
                    BTCXPubMap.keySet()) {
                String path = hdPath.toUpperCase();
                if (hdPath.startsWith(key)) {
                    BTCDeriver deriver = BTCXPubMap.get(key);
                    String rest = path.replace(key, "");
                    int change = Integer.parseInt(rest.split("/")[0]);
                    int addressIndex = Integer.parseInt(rest.split("/")[1]);
                    assert deriver != null;
                    String derivedAddress = deriver.derive(change, addressIndex);
                    if (!derivedAddress.equalsIgnoreCase(address)) {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    private void feeAttackChecking(TxEntity txEntity) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String inputs = txEntity.getFrom();
            String outputs = txEntity.getTo();
            List<TxEntity> txs = repository.loadAllTxSync(Coins.BTC.coinId());
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
        if (coinCode.startsWith("BTC")) {
            tx.setFrom(getBTCInputs().toString());
        } else {
            tx.setFrom(getFromAddress());
        }
        tx.setTo(getToAddress());
        tx.setAmount(nf.format(transaction.getAmount()) + " " + transaction.getUnit());
        tx.setFee(nf.format(transaction.getFee()) + " " + tx.getDisplayName());
        tx.setMemo(transaction.getMemo());
        tx.setBelongTo(repository.getBelongTo());
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
            AddressEntity addressEntity = repository.loadAddressBypath(transaction.getHdPath());
            return addressEntity.getAddressString();
        }
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String[] externalPath = Stream.of(paths)
                .filter(this::isExternalPath)
                .toArray(String[]::new);
        ensureAddressExist(externalPath);
        JSONArray array = deriveAddresses(paths);

        return Stream.of(externalPath)
                .distinct()
                .map(path -> repository.loadAddressByPathAndCoinId(path, Coins.coinIdFromCoinCode(coinCode)).getAddressString())
                .reduce((s1, s2) -> s1 + AbsTx.SEPARATOR + s2)
                .orElse("");
    }

    private JSONArray getBTCInputs() {
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String[] externalPath = Stream.of(paths)
                .filter(this::isExternalPath)
                .toArray(String[]::new);
        ensureAddressExist(externalPath);
        JSONArray array = deriveAddresses(paths);
        return array;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private boolean isExternalPath(@NonNull String path) {
        try {
            return CoinPath.parsePath(path).getParent().isExternal();
        } catch (Exception e) {
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
        AddressEntity address = repository.loadAddressByPathAndCoinId(maxIndexHdPath, Coins.coinIdFromCoinCode(coinCode));
        if (address == null) {
            addAddress(getAddressIndex(maxIndexHdPath));
        }
    }

    protected void addAddress(int addressIndex) {
        CoinEntity coin = repository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode));
        int addressLength = repository.loadAccountsForCoin(coin).get(0).getAddressLength();

        if (addressLength < addressIndex + 1) {
            String[] names = new String[addressIndex + 1 - addressLength];
            int index = 0;
            for (int i = addressLength; i < addressIndex + 1; i++) {
                names[index++] = coinCode + "-" + i;
            }
            final CountDownLatch mLatch = new CountDownLatch(1);
            addingAddress.postValue(true);
            new AddAddressViewModel.AddAddressTask(coin, repository, mLatch::countDown)
                    .execute(names);
            try {
                mLatch.await();
                addingAddress.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class BTCDeriver {
        private final String xPub;
        private final AbsDeriver deriver;

        public BTCDeriver(String xPub, AbsDeriver deriver) {
            this.xPub = xPub;
            this.deriver = deriver;
        }

        public String derive(int change, int address) {
            return deriver.derive(xPub, change, address);
        }
    }

    private void initBTCXPubMap() {
        if (this.BTCXPubMap.isEmpty()) {
            this.BTCXPubMap.put(BTC_SEGWIT_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC.coinCode())));
            this.BTCXPubMap.put(BTC_LEGACY_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC_LEGACY.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC_LEGACY.coinCode())));
            this.BTCXPubMap.put(BTC_NATIVE_SEGWIT_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC_NATIVE_SEGWIT.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC_NATIVE_SEGWIT.coinCode())));

            if (FeatureFlags.ENABLE_XTN) {
                this.BTCXPubMap.put(BTC_TESTNET_SEGWIT_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC_TESTNET_SEGWIT.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC_TESTNET_SEGWIT.coinCode())));
                this.BTCXPubMap.put(BTC_TESTNET_LEGACY_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC_TESTNET_LEGACY.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC_TESTNET_LEGACY.coinCode())));
                this.BTCXPubMap.put(BTC_TESTNET_NATIVE_SEGWIT_PATH, new BTCDeriver(repository.loadCoinSync(Coins.BTC_TESTNET_NATIVE_SEGWIT.coinId()).getExPub(), AbsDeriver.newInstance(Coins.BTC_TESTNET_NATIVE_SEGWIT.coinCode())));
            }
        }
    }

    public JSONArray deriveAddresses(String[] paths) {
        initBTCXPubMap();
        JSONArray jsonArray = new JSONArray();
        BTCXPubMap.keySet().forEach(key -> {
            Arrays.stream(paths).forEach(path -> {
                path = path.toUpperCase();
                if (path.startsWith(key)) {
                    BTCDeriver deriver = BTCXPubMap.get(key);
                    String rest = path.replace(key, "");
                    int change = Integer.parseInt(rest.split("/")[0]);
                    int addressIndex = Integer.parseInt(rest.split("/")[1]);
                    assert deriver != null;
                    String address = deriver.derive(change, addressIndex);
                    JSONObject object = new JSONObject();
                    try {
                        object.put("isChange", change == 1);
                        object.put("address", address);
                        object.put("path", path);
                        jsonArray.put(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        return jsonArray;
    }

    public LiveData<Boolean> getAddingAddressState() {
        return addingAddress;
    }

    private int getAddressIndex(String hdPath) {
        try {
            return CoinPath.parsePath(hdPath).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer[] signers = initSigners();
            signTransaction(signers);
        });
    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        return observableTransaction.getValue().getSignResult();
    }

    void signTransaction(Signer... signer) {
        SignCallback callback = initSignTxCallback();
        callback.startSign();
        if (signer == null) {
            callback.onFail();
            return;
        }
        switch (transaction.getTxType()) {
            case "DOT":
                Dot dot = new Dot(new DotImpl(coinCode));
                dot.generateTransaction(transaction, callback, signer);
                break;
            case "ETH":
                Eth eth = new Eth(new EthImpl(Eth.CHAIN_ID));
                eth.generateTransaction(transaction, callback, signer);
                break;
            case "XRP":
                Xrp xrp = new Xrp(new XrpImpl());
                xrp.generateTransaction(transaction, callback, signer);
                break;
            case "TRON":
                Tron tron = new Tron(new TronImpl());
                tron.generateTransaction(transaction, callback, signer);
                break;
            default:
                AbsCoin coin = AbsCoin.newInstance(coinCode);
                Objects.requireNonNull(coin).generateTransaction(transaction, callback, signer);
        }
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
                onSignSuccess(txId, rawTx);
                new ClearTokenCallable().call();
            }

            @Override
            public void postProgress(int progress) {

            }
        };
    }

    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTransaction.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        tx.setSignedHex(rawTx);
        repository.insertTx(tx);
        return tx;
    }

    private Signer[] initSigners() {
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String coinCode = transaction.getCoinCode();
        String[] distinctPaths = Stream.of(paths).distinct().toArray(String[]::new);
        Signer[] signer = new Signer[distinctPaths.length];
        boolean shouldProvidePublicKey = Signer.shouldProvidePublicKey(transaction.getCoinCode());
        String exPub = null;
        if (shouldProvidePublicKey) {
            exPub = repository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode)).getExPub();
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
        return Objects.requireNonNull(observableTransaction.getValue()).getTxId();
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableTransaction.getValue()).getSignedHex();
    }

    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public boolean isAddressInWhiteList(String address) {
        try {
            return sExecutor.submit(() -> repository.queryWhiteList(address) != null).get();
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

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }
}
