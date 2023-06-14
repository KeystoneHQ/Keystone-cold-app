package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.ClearTokenCallable;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.cryptocore.CardanoProtoc;
import com.keystone.cold.cryptocore.CardanoService;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.RustSigner;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin.PSBT;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoCertificate;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoTransaction;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoUTXO;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.cardano.CardanoSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CardanoTxViewModel extends BaseTxViewModel<CardanoTransaction> {
    private static final String TAG = "CardanoTxViewModel";

    public CardanoTxViewModel(@NonNull Application application) {
        super(application);
    }

    private String requestId;
    private String origin;
    private String signData;
    private List<String> myPaths;
    private String signed;
    private String parsedJson;

    private String prefixPath(String path) {
        if (path.startsWith("/")) return "m" + path;
        if (!path.toLowerCase().startsWith("m/")) {
            return "m/" + path;
        }
        return path;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            isParsing.postValue(true);
            String transactionData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
            ArrayList<CardanoUTXO> utxos = (ArrayList<CardanoUTXO>) bundle.getSerializable(BundleKeys.CARDANO_UTXO_KEY);
            ArrayList<CardanoCertificate> certificates = (ArrayList<CardanoCertificate>) bundle.getSerializable(BundleKeys.CARDANO_CERTIFICATE_KEY);
            String masterFingerprint = new GetMasterFingerprintCallable().call();
            List<CryptoKeypath> cryptoKeypaths = new ArrayList<>();
            utxos.forEach(v -> cryptoKeypaths.add(v.getPath()));
            certificates.forEach(v -> cryptoKeypaths.add(v.getKeypath()));
            List<CryptoKeypath> myPaths = cryptoKeypaths.stream().filter(v -> Hex.toHexString(v.getSourceFingerprint()).equalsIgnoreCase(masterFingerprint)).collect(Collectors.toList());
            if (myPaths.size() == 0) {
                observableException.postValue(InvalidTransactionException.newInstance("transaction not related to this wallet"));
                isParsing.postValue(false);
                return;
            }
            this.myPaths = myPaths.stream().map(v -> prefixPath(v.getPath())).collect(Collectors.toList());
            CryptoKeypath first = myPaths.get(0);
            String keypath = first.getPath();
            if (!keypath.toUpperCase().startsWith("M/")) {
                keypath = "M/" + keypath;
            }
            String xpub = CardanoViewModel.getXPubByPath(keypath, repository);
            List<CardanoProtoc.CardanoUtxo> cardanoUtxos = new ArrayList<>();
            utxos.forEach(v -> {
                CardanoProtoc.CardanoUtxo.Builder builder = CardanoProtoc.CardanoUtxo.newBuilder();
                builder.setIndex(v.getIndex());
                builder.setPath(prefixPath(v.getPath().getPath()));
                builder.setAddress(v.getAddress());
                builder.setTransactionHash(Hex.toHexString(v.getTransactionHash()));
                builder.setValue(v.getValue());
                builder.setMasterFingerprint(Hex.toHexString(v.getPath().getSourceFingerprint()));
                cardanoUtxos.add(builder.build());
            });
            List<CardanoProtoc.CardanoCertKey> cardanoCertKeys = new ArrayList<>();
            certificates.forEach(v -> {
                CardanoProtoc.CardanoCertKey.Builder builder = CardanoProtoc.CardanoCertKey.newBuilder();
                builder.setPath(prefixPath(v.getKeypath().getPath()));
                builder.setMasterFingerprint(Hex.toHexString(v.getKeypath().getSourceFingerprint()));
                builder.setKeyHash(Hex.toHexString(v.getKeyHash()));
                cardanoCertKeys.add(builder.build());
            });
            parsedJson = CardanoService.parseTransaction(transactionData, xpub, masterFingerprint, cardanoUtxos, cardanoCertKeys);
            try {
                CardanoTransaction transaction = CardanoTransaction.fromJSON(parsedJson);
                signData = transaction.getSignData();
                observableTransaction.postValue(transaction);
            } catch (JSONException e) {
                observableException.postValue(InvalidTransactionException.newInstance("invalid transaction"));
                e.printStackTrace();
            }
            isParsing.postValue(false);
        });
    }

    public void parseExistingTransaction(String txId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                isParsing.postValue(true);
                TxEntity tx = repository.loadTxSync(txId);
                signed = tx.getSignedHex();
                JSONObject object = new JSONObject(tx.getAddition());
                parsedJson = object.getString("rawTx");
                CardanoTransaction transaction = CardanoTransaction.fromJSON(parsedJson);
                transaction.setSigned(signed);
                observableTransaction.postValue(transaction);
            } catch (JSONException e) {
                observableException.postValue(InvalidTransactionException.newInstance("invliad transaction"));
                e.printStackTrace();
            } finally {
                isParsing.postValue(false);
            }
        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }

    @Override
    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            CardanoTransaction cardanoTransaction = observableTransaction.getValue();
            if (cardanoTransaction == null) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            signState.postValue(STATE_SIGNING);
            List<RustSigner> signers = initSigners();
            if (signers == null) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            List<CardanoProtoc.CardanoSignature> signatures = new ArrayList<>();
            for (RustSigner signer : signers) {
                String signature = signer.signADA(signData);
                if (signature != null) {
                    CardanoProtoc.CardanoSignature.Builder builder = CardanoProtoc.CardanoSignature.newBuilder();
                    builder.setPublicKey(signer.getPublicKey());
                    builder.setSignature(signature);
                    signatures.add(builder.build());
                } else {
                    signState.postValue(STATE_SIGN_FAIL);
                    new ClearTokenCallable().call();
                    return;
                }
            }
            String witnessSet = CardanoService.composeWitnessSet(signatures);
            UUID uuid = UUID.fromString(this.requestId);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            CardanoSignature signature = new CardanoSignature(requestId, Hex.decode(witnessSet));
            signed = signature.toUR().toString();
            try {
                insertDB(signed, signData);
            } catch (JSONException e) {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
                return;
            }
            signState.postValue(STATE_SIGN_SUCCESS);
            new ClearTokenCallable().call();
        });
    }

    private List<RustSigner> initSigners() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG, "authToken null");
            return null;
        }
        Set<String> set = new HashSet<>(myPaths);
        List<String> uniqueList = new ArrayList<>(set);
        List<RustSigner> signers = new ArrayList<>();
        for (String myPath : uniqueList) {
            AccountEntity accountEntity = CardanoViewModel.getAccountByPath(myPath, repository);
            String subPath = myPath.toLowerCase().replace(accountEntity.getHdPath().toLowerCase(), "");
            subPath = prefixPath(subPath);
            String pubkey = CardanoService.derivePublicKey(accountEntity.getExPub(), subPath);
            signers.add(new RustSigner(myPath.toLowerCase(), authToken, pubkey));
        }
        return signers;
    }

    private void insertDB(String signed, String txId) throws JSONException {
        TxEntity txEntity = generateTxEntity();
        txEntity.setTxId(txId);
        txEntity.setSignedHex(signed);
        txEntity.setAddition(
                new JSONObject().put("rawTx", parsedJson)
                        .put("requestId", requestId)
                        .toString()
        );
        repository.insertTx(txEntity);
    }

    private TxEntity generateTxEntity() {
        TxEntity txEntity = new TxEntity();
        txEntity.setCoinId(Coins.ADA.coinId());
        //update origin
        txEntity.setSignId(Wallet.ETERNL_WALLET_SIGN_ID);
        txEntity.setCoinCode(Coins.ADA.coinCode());
        txEntity.setTimeStamp(getUniversalSignIndex(getApplication()));
        txEntity.setBelongTo(repository.getBelongTo());
        return txEntity;
    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        return signed;
    }
}
