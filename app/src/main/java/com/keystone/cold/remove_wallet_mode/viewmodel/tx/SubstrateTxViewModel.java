package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate.SubstrateTransaction;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SubstrateTxViewModel extends BaseTxViewModel<SubstrateTransaction> {
    String coinCode;
    protected final DataRepository mRepository;

    private TxEntity txEntity;
    private AddressEntity addressEntity;

    public SubstrateTxViewModel(@NonNull Application application) {
        super(application);
        mRepository = MainApplication.getApplication().getRepository();
    }

    public void reset() {
        coinCode = null;
        txEntity = null;
        addressEntity = null;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                isParsing.postValue(true);
                String data = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                String parsedTransaction = bundle.getString(BundleKeys.PARSED_TRANSACTION_KEY);
                JSONObject pt = new JSONObject(parsedTransaction);
                SubstrateTransaction substrateTransaction = SubstrateTransaction.factory(parsedTransaction, data);
                rawFormatTx.postValue(substrateTransaction.getType().equals("Stub")? "Transaction too large": data);
                coinCode = substrateTransaction.getCoinCode();
                generateAndPostSubstrateTxV2(pt, data);
                observableTransaction.postValue(substrateTransaction);
            } catch (JSONException e) {
                e.printStackTrace();
                observableException.postValue(new InvalidTransactionException(MainApplication.getApplication().getString(R.string.incorrect_tx_data), "invalid observableTransaction"));
            } catch (InvalidAccountException | InvalidTransactionException e) {
                observableException.postValue(e);
            } finally {
                isParsing.postValue(false);
            }
        });
    }

    public void parseExistingTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                isParsing.postValue(true);
                TxEntity txEntity = mRepository.loadTxSync(txId);
                JSONObject addition = new JSONObject(txEntity.getAddition());
                String parsedTransaction = addition.getString("parsed_transaction");
                String rawTx = addition.getString("raw_message");
                rawFormatTx.postValue(rawTx);
                SubstrateTransaction substrateTransaction = SubstrateTransaction.factory(parsedTransaction, rawTx);
                coinCode = substrateTransaction.getCoinCode();
                substrateTransaction.setSignedHex(txEntity.getSignedHex());
                observableTransaction.postValue(substrateTransaction);
            } catch (JSONException e) {
                e.printStackTrace();
                observableException.postValue(new InvalidTransactionException(MainApplication.getApplication().getString(R.string.incorrect_tx_data), "invalid observableTransaction"));
            } catch (InvalidTransactionException e) {
                observableException.postValue(e);
            }finally {
                isParsing.postValue(false);
            }
        });
    }

    public void generateAndPostSubstrateTxV2(JSONObject parsedTransaction, String transactionHex) throws JSONException, InvalidAccountException {
        String type = parsedTransaction.getString("transaction_type");
        if (type.equals("Sign")) {
            JSONObject authorInfo = parsedTransaction.getJSONObject("author_info");
            String from = authorInfo.getString("base58");

            addressEntity = findAccount(from);
            if (addressEntity == null) {
                throw new InvalidAccountException(getApplication().getString(R.string.incorrect_tx_data), "invalid account");
            }

            TxEntity tx = new TxEntity();
            tx.setSignId(Wallet.UNKNOWN_WALLET_SIGN_ID);
            tx.setTimeStamp(getUniversalSignIndex(getApplication()));
            tx.setCoinCode(coinCode);
            tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
            tx.setFrom(from);
            tx.setFee("");
            tx.setSignedHex("");
            tx.setBelongTo(mRepository.getBelongTo());

            JSONObject addition = new JSONObject();
            addition.put("parsed_transaction", parsedTransaction);
            addition.put("raw_message", transactionHex);
            tx.setAddition(addition.toString());
            txEntity = tx;
        } else {
        }
    }

    public AddressEntity findAccount(String account) {
        Future<AddressEntity> future = Executors.newSingleThreadExecutor().submit(() -> {
            List<AddressEntity> allSubstrateAddress = new ArrayList<>();
            allSubstrateAddress.addAll(mRepository.loadAddressSync(Coins.KSM.coinId()));
            allSubstrateAddress.addAll(mRepository.loadAddressSync(Coins.DOT.coinId()));
            Optional<AddressEntity> optionalAddressEntity = allSubstrateAddress.stream().filter(addressEntity -> addressEntity.getAddressString().equals(account)).findAny();
            return optionalAddressEntity.orElse(null);
        });
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCoinCode() {
        return coinCode;
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }

    @Override
    public void handleSign() {
        // we do not use this method for substrate transactions;
    }

    public void handleSignContent(String signContent) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            String authToken = getAuthToken();
            if (TextUtils.isEmpty(authToken)) {
                callback.onFail();
            }
            Signer signer = new ChipSigner(addressEntity.getPath(), authToken);

            byte[] content = Hex.decode(signContent);
            String dataToSign = signContent;
            if (content.length > 256) {
                byte[] hash = AddressCodec.blake2b(content, 256);
                dataToSign = Hex.toHexString(hash);
            }
            String signedHex = signer.sign(dataToSign);
            String txId = signedHex.substring(0, 32);
            if (!TextUtils.isEmpty(signedHex)) {
                callback.onSuccess(txId, signedHex);
            } else {
                callback.onFail();
            }
        });
    }

    @Override
    public void handleSignMessage() {
        // Do not support this on Substrate;
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

    protected TxEntity onSignSuccess(String txId, String signature) {
        Objects.requireNonNull(txEntity).setTxId(txId);
        txEntity.setSignedHex("01"+signature);
        mRepository.insertTx(txEntity);
        return txEntity;
    }

    @Override
    public String getSignatureUR() {
        // we do not use this on substrate
        return null;
    }

    public String getSignedHex() {
        return txEntity.getSignedHex();
    }
}
