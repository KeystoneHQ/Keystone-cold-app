package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.cryptocore.SolanaParser;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.helper.address_generators.SolanaAddressGenerator;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.sparrowwallet.hummingbird.registry.solana.SolSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;


public class SolanaTxViewModel extends BaseTxViewModel<JSONObject> {


    private static final String TAG = "SolanaTxViewModel";
    private String txHex;
    private String messageData;
    private String hdPath;
    private String origin;


    private String requestId;
    private String signature;

    private String parsedMessage;


    public SolanaTxViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);

            parsedMessage = SolanaParser.parse(txHex);
            if (parsedMessage != null) {
                try {
                    ensureAddressExist(hdPath);
                    JSONObject jsonObject = new JSONObject(parsedMessage);
                    observableTransaction.postValue(jsonObject);
                    rawFormatTx.postValue(jsonObject.toString(2));
                } catch (JSONException | InvalidAccountException e) {
                    e.printStackTrace();
                    rawFormatTx.postValue("");
                    observableTransaction.postValue(null);
                    observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
                }
            } else {
                Log.e(TAG, "parse solana transaction failed");
                rawFormatTx.postValue("");
                observableTransaction.postValue(null);
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        MutableLiveData<JSONObject> observableObject = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                messageData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
                hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
                requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
                String fromAddress = getFromAddress(hdPath);
                JSONObject object = new JSONObject();
                object.put("data", messageData);
                object.put("fromAddress", fromAddress);
                observableObject.postValue(object);
            } catch (JSONException | InvalidAccountException e) {
                e.printStackTrace();
                observableObject.postValue(null);
                observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
        return observableObject;
    }

    private String getFromAddress(String path) throws InvalidAccountException {
        ensureAddressExist(path);
        AddressEntity addressEntity = repository.loadAddressBypath(path);
        if (addressEntity != null) {
            return addressEntity.getAddressString();
        }
        return "";
    }

    private Signer initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        return new ChipSigner(hdPath.toLowerCase(), authToken);
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signTransaction(signer);
        });
    }

    private void signTransaction(Signer signer) {
        signCallBack.startSign();
        SignTxResult result = new SolImpl().signHex(txHex, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result.signaturHex;
            insertDB(signature, txHex, parsedMessage);
            signCallBack.onSignTxSuccess();
        }
    }

    @Override
    public void handleSignMessage() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer signer = initSigner();
            signMessage(signer);
        });
    }

    private void signMessage(Signer signer) {
        signCallBack.startSign();
        String result = new SolImpl().signMessage(messageData, signer);
        if (result == null) {
            signCallBack.onFail();
        } else {
            signature = result;
            signCallBack.onSignMsgSuccess();
        }
    }

    @Override
    public String getSignatureUR() {
        if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(requestId)) {
            return "";
        }
        byte[] signatureByte = Hex.decode(signature);
        UUID uuid = UUID.fromString(requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        SolSignature solSignature = new SolSignature(signatureByte, requestId);
        return solSignature.toUR().toString();
    }

    public void parseTransactionFromRecord(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TxEntity txEntity = repository.loadTxSync(txId);
            parseSolanaTxEntity(txEntity);
        });
    }

    private void parseSolanaTxEntity(TxEntity txEntity) {
        String addition = txEntity.getAddition();
        if (TextUtils.isEmpty(addition)) {
            rawFormatTx.postValue("");
            observableTransaction.postValue(null);
            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
            return;
        }
        try {
            JSONObject root = new JSONObject(addition);
            JSONObject additions = root.getJSONObject("additions");
            String coin = additions.getString("coin");
            if (!TextUtils.isEmpty(coin) && coin.equals(Coins.SOL.coinId())) {
                String parsedMessage = additions.getJSONObject("addition").getString("parsed_message");
                JSONObject jsonObject = new JSONObject(parsedMessage);
                rawFormatTx.postValue(jsonObject.toString(2));
                jsonObject.put("record", true);
                jsonObject.put("signatureUR", txEntity.getSignedHex());
                observableTransaction.postValue(jsonObject);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
            rawFormatTx.postValue("");
            observableTransaction.postValue(null);
            observableException.postValue(new InvalidTransactionException(getApplication().getString(R.string.incorrect_tx_data), "invalid transaction"));
        }
    }

    private void ensureAddressExist(String path) throws InvalidAccountException {
        path = path.toUpperCase();
        SOLAccount target = SOLAccount.getAccountByPath(path);
        if (target == null) {
            throw new InvalidAccountException(getApplication().getString(R.string.incorrect_tx_data), "not have match account");
        }
        AddressEntity address = repository.loadAddressBypath(path);
        if (address == null) {
            int addressIndex = getAddressIndex(target, path);
            if (addressIndex != -1) {
                AccountEntity accountEntity = repository.loadTargetSOLAccount(target);
                if (accountEntity == null) {
                    throw new InvalidAccountException(getApplication().getString(R.string.incorrect_tx_data), "not have match account");
                }
                int addressNum = addressIndex - accountEntity.getAddressLength() + 1;
                if (addressNum > 0) {
                    new SolanaAddressGenerator(target.getCode()).generateAddress(addressNum);
                }
            }
        }
    }

    private int getAddressIndex(SOLAccount account, String path) {
        return account.getAddressIndexByPath(path);
    }

    private void insertDB(String signature, String rawMessage, String parsedMessage) {
        TxEntity txEntity = generateSolTxEntity();
        txEntity.setTxId(signature);
        String additionsString = null;
        try {
            JSONObject addition = new JSONObject();
            addition.put("signature", signature);
            addition.put("raw_message", rawMessage);
            addition.put("parsed_message", parsedMessage);
            addition.put("sign_by", SOLAccount.getAccountByPath(hdPath).getCode());
            JSONObject additions = new JSONObject();
            additions.put("coin", Coins.SOL.coinId());
            additions.put("addition", addition);
            JSONObject root = new JSONObject();
            root.put("additions", additions);
            additionsString = root.toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        if (!TextUtils.isEmpty(additionsString)) {
            //addition结构详见 com.keystone.cold.db.entity.TxEntity addition字段
            txEntity.setAddition(additionsString);
            repository.insertTx(txEntity);
        }
    }

    private TxEntity generateSolTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.SOL.coinId());
        txEntity.setSignId(getSignId());
        txEntity.setCoinCode(Coins.SOL.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        txEntity.setSignedHex(getSignatureUR());
        return txEntity;
    }


    // Current only SOLFLARE support solana.
    // If add other wallets in the future, need to rewrite
    private String getSignId() {
        return Wallet.SOLFLARE.getSignId();
    }

}
