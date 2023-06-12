package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.cryptocore.CardanoProtoc;
import com.keystone.cold.cryptocore.CardanoService;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoCertificate;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoTransaction;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano.CardanoUTXO;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardanoTxViewModel extends BaseTxViewModel<CardanoTransaction> {


    public CardanoTxViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            isParsing.postValue(true);
            String transactionData = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            String requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            String origin = bundle.getString(BundleKeys.SIGN_ORIGIN_KEY);
            ArrayList<CardanoUTXO> utxos = (ArrayList<CardanoUTXO>) bundle.getSerializable(BundleKeys.CARDANO_UTXO_KEY);
            ArrayList<CardanoCertificate> certificates = (ArrayList<CardanoCertificate>) bundle.getSerializable(bundle.getString(BundleKeys.CARDANO_CERTIFICATE_KEY));
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
                builder.setPath(v.getPath().getPath());
                builder.setAddress(v.getAddress());
                builder.setTransactionHash(Hex.toHexString(v.getTransactionHash()));
                builder.setValue(v.getValue());
                builder.setMasterFingerprint(Hex.toHexString(v.getPath().getSourceFingerprint()));
                cardanoUtxos.add(builder.build());
            });
            List<CardanoProtoc.CardanoCertKey> cardanoCertKeys = new ArrayList<>();
            certificates.forEach(v -> {
                CardanoProtoc.CardanoCertKey.Builder builder = CardanoProtoc.CardanoCertKey.newBuilder();
                builder.setPath(v.getKeypath().getPath());
                builder.setMasterFingerprint(Hex.toHexString(v.getKeypath().getSourceFingerprint()));
                builder.setKeyHash(Hex.toHexString(v.getKeyHash()));
                cardanoCertKeys.add(builder.build());
            });
            String parsed = CardanoService.parseTransaction(transactionData, xpub, masterFingerprint, cardanoUtxos, cardanoCertKeys);

        });
    }

    @Override
    public MutableLiveData<JSONObject> parseMessage(Bundle bundle) {
        return null;
    }

    @Override
    public void handleSign() {

    }

    @Override
    public void handleSignMessage() {

    }

    @Override
    public String getSignatureUR() {
        return null;
    }
}
