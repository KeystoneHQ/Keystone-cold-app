package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.coins.XRP.SupportTransactions;
import com.keystone.coinlib.coins.XRP.Xrp;
import com.keystone.coinlib.coins.XRP.XrpImpl;
import com.keystone.coinlib.coins.XRP.XrpTransaction;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidXRPAccountException;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Objects;

public class RippleTxViewModel extends BaseTxViewModel<JSONObject>{
    private String coinCode;

    private JSONObject xummTxObj;
    private String account;
    private String signingPubKey;
    private String signingKeyPath;

    private TxEntity txEntity;
    public RippleTxViewModel(@NonNull Application application) {
        super(application);
        coinCode = Coins.XRP.coinCode();
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String json = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                JSONObject object = new JSONObject(json);
                xummTxObj = object;
                rawFormatTx.postValue(json);
                XrpTransaction xrpTransaction = SupportTransactions.get(object.getString("TransactionType"));

                if (xrpTransaction == null || !xrpTransaction.isValid(object)) {
                    observableException.postValue(InvalidTransactionException.newInstance("invalid xrp exception"));
                    return;
                }
                account = xummTxObj.optString("Account");
                signingPubKey = xummTxObj.optString("SigningPubKey");
                if (!isValidAccount()) {
                    observableException.postValue(InvalidXRPAccountException.newInstance("cannot derive provided xrp account by provided signing key"));
                    return;
                }
                if (!checkAccount()) {
                    observableException.postValue(InvalidXRPAccountException.newInstance("provided xrp account does not any xrp account in this wallet"));
                    return;
                }
                observableTransaction.postValue(xrpTransaction.flatTransactionDetail(object));
                TxEntity tx = new TxEntity();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                tx.setCoinCode(coinCode);
                tx.setTimeStamp(getUniversalSignIndex(getApplication()));
                // how to diff XRP origin?
                tx.setSignId(Wallet.UNKNOWN_WALLET_SIGN_ID);
                tx.setCoinId(Coins.XRP.coinId());
                tx.setBelongTo(repository.getBelongTo());
                txEntity = tx;
            }catch (JSONException e){
                e.printStackTrace();
            }
        });
    }

    public boolean isValidAccount() {
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(signingPubKey)) {
            return false;
        }
        return Xrp.encodeAccount(signingPubKey).equals(account);
    }

    public boolean checkAccount() {
        for (AddressEntity address : repository.loadAddressSync(Coins.XRP.coinId())) {
            if (address.getAddressString().equals(account)) {
                signingKeyPath = address.getPath().toLowerCase();
                return true;
            }
        }
        return false;
    }

    public void parseExistingTransaction(String txId){
        AppExecutors.getInstance().diskIO().execute(() -> {
            txEntity = repository.loadTxSync(txId);
            try {
                rawFormatTx.postValue(txEntity.getSignedHex());
                xummTxObj = new JSONObject(txEntity.getSignedHex());
                XrpTransaction xrpTransaction = SupportTransactions.get(xummTxObj.getString("TransactionType"));
                JSONObject displayTx = xrpTransaction.flatTransactionDetail(xummTxObj);
                displayTx.put("txHex", xummTxObj.getString("txHex"));
                observableTransaction.postValue(displayTx);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
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

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            Signer signer = new ChipSigner(signingKeyPath, getAuthToken(), signingPubKey);
            XrpImpl xrp = new XrpImpl();
            xrp.generateJsonTransaction(xummTxObj, callback, signer);
        });
    }

    protected TxEntity onSignSuccess(String txId, String rawTx) {
        Objects.requireNonNull(txEntity).setTxId(txId);
        try {
            xummTxObj.put("txHex", rawTx);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        txEntity.setSignedHex(xummTxObj.toString());
        repository.insertTx(txEntity);
        return txEntity;
    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        return null;
    }

    public String getSignedTxHex( ) {
        try {
            JSONObject object = new JSONObject(txEntity.getSignedHex());
            return object.getString("txHex");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
