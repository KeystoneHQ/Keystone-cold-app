/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.viewmodel.tx;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.polkadot.UOS.Extrinsic;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.coinlib.exception.InvalidUOSException;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PolkadotJsTxConfirmViewModel extends Base {

    private final DataRepository mRepo;
    private String txId;

    private AddressEntity addressEntity;

    public PolkadotJsTxConfirmViewModel(@NonNull Application application) {
        super(application);
        mRepo = ((MainApplication) application).getRepository();
    }

    private JSONObject extrinsicObject;

//    public void parseTxData(String data) {
//        try {
//            SubstratePayload sp = new SubstratePayload(data);
//            isHash = sp.isHash;
//            extrinsic = sp.extrinsic;
//            accountPublicKey = sp.accountPublicKey;
//            extrinsicObject = extrinsic.palletParameter.toJSON();
//            TxEntity tx = generateSubstrateTxEntity(sp);
//            observableTx.postValue(tx);
//            signingPayload = sp.getSigningPayload();
//        } catch (InvalidUOSException | JSONException | DecoderException e) {
//            e.printStackTrace();
//        }
//    }

    public TxEntity generateAndPostSubstrateTxV2(JSONObject parsedTransaction, String transactionHex) throws JSONException, InvalidAccountException {
        String type = parsedTransaction.getString("transaction_type");
        if (type.equals("Sign")) {
            JSONObject networkInfo = parsedTransaction.getJSONObject("network_info");
            String network = networkInfo.getString("network_title");
            switch (network) {
                case "Polkadot": {
                    coinCode = Coins.DOT.coinCode();
                    break;
                }
                case "Kusama": {
                    coinCode = Coins.KSM.coinCode();
                    break;
                }
                default: {
                    coinCode = Coins.DOT.coinCode();
                }
            }
            JSONObject authorInfo = parsedTransaction.getJSONObject("author_info");
            String from = authorInfo.getString("base58");

            addressEntity = findAccount(from);
            if (addressEntity == null) {
                throw new InvalidAccountException("invalid account");
            }

            TxEntity tx = new TxEntity();
            tx.setSignId(WatchWallet.POLKADOT_JS_SIGN_ID);
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
            observableTx.postValue(tx);
            return tx;
        } else {
            return null;
        }
    }

//    public boolean isTransactionSupported(Parameter parameter) {
//        if (parameter == null) return false;
//        return parameter.name.startsWith("balance.transfer")
//                || parameter.name.startsWith("staking")
//                || parameter.name.startsWith("utility.batch")
//                || parameter.name.startsWith("session.setKeys")
//                || parameter.name.startsWith("democracy")
//                || parameter.name.equals("identity.setIdentity")
//                || parameter.name.equals("proxy.addProxy")
//                || parameter.name.startsWith("electionsPhragmen")
//                || parameter.name.startsWith("treasury")
//                || parameter.name.startsWith("society")
//                || parameter.name.startsWith("recovery")
//                || parameter.name.startsWith("multisig");
//    }
//
//    public boolean isNetworkSupported(Network network) {
//        return Network.supportedNetworks.contains(network);
//    }

    public AddressEntity findAccount(String account) {
        Future<AddressEntity> future = Executors.newSingleThreadExecutor().submit(() -> {
            List<AddressEntity> allSubstrateAddress = new ArrayList<>();
            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.KSM.coinId()));
            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.DOT.coinId()));
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

//    public boolean isAccountMatch(String account) {
//        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
//            List<AddressEntity> allSubstrateAddress = new ArrayList<>();
//            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.KSM.coinId()));
//            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.DOT.coinId()));
//            return allSubstrateAddress.stream().anyMatch(entity -> account.equals(entity.getAddressString()));
//        });
//        try {
//            return future.get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    private TxEntity generateSubstrateTxEntity(SubstratePayload sp) {
//        TxEntity tx = new TxEntity();
//        coinCode = sp.network.coinCode();
//        tx.setSignId(WatchWallet.POLKADOT_JS_SIGN_ID);
//        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
//        tx.setCoinCode(coinCode);
//        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
//        tx.setFrom(sp.getAccount());
//        tx.setFee(sp.extrinsic.getTip() + " " + coinCode);
//        tx.setSignedHex(extrinsicObject.toString());
//        tx.setBelongTo(mRepository.getBelongTo());
//        return tx;
//    }

    public void handleSign(String signContent) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            String authToken = getAuthToken();
            if (TextUtils.isEmpty(authToken)) {
                Log.w(TAG, "authToken null");
                callback.onFail();
            }
            Signer signer = new ChipSigner(addressEntity.getPath(), authToken);
            String signedHex = signer.sign(signContent);
            String txId = signedHex.substring(0, 32);
            if (!TextUtils.isEmpty(signedHex)) {
                callback.onSuccess(txId, signedHex);
            } else {
                callback.onFail();
            }
        });
    }

    @Override
    public String getTxId() {
        return txId;
    }

    protected TxEntity onSignSuccess(String txId, String signature) {
        this.txId = txId;
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        tx.setSignedHex("01"+signature);
        mRepository.insertTx(tx);
        return tx;
    }
}
