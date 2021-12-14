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
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.BTC.Btc;
import com.keystone.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.keystone.coinlib.coins.BTC.Electrum.TransactionInput;
import com.keystone.coinlib.coins.BTC.Electrum.TransactionOutput;
import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.path.Account;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.db.entity.AccountEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.ui.modal.ExportToSdcardDialog;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.update.utils.FileUtils;
import com.keystone.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ElectrumViewModel extends AndroidViewModel {

    public static final String ELECTRUM_SIGN_ID = "electrum_sign_id";
    private static final int DEFAULT_CHANGE_ADDRESS_NUM = 100;

    private static final Pattern signedTxnPattern = Pattern.compile("^signed_[0-9a-fA-F]{8}.txn$");
    private final DataRepository mRepo;
    private final MutableLiveData<String> exPub = new MutableLiveData<>();
    private final Storage storage;
    private final MutableLiveData<List<String>> changeAddress = new MutableLiveData<>();
    private String xpub;

    public ElectrumViewModel(@NonNull Application application) {
        super(application);
        mRepo = MainApplication.getApplication().getRepository();
        storage = Storage.createByEnvironment();
        deriveChangeAddress();
    }

    private void deriveChangeAddress() {
        AppExecutors.getInstance().networkIO().execute(()->{
            if (TextUtils.isEmpty(xpub)) {
                xpub = new ExpubInfo().invoke().expub;
            }
            List<String> changes = new ArrayList<>();
            AbsDeriver btcDeriver = new Btc.Deriver();
            for (int i = 0; i < DEFAULT_CHANGE_ADDRESS_NUM; i++) {
                changes.add(btcDeriver.derive(xpub,1, i));
            }
            changeAddress.postValue(changes);
        });
    }

    public LiveData<List<String>> getChangeAddress() {
        return changeAddress;
    }

    public static boolean writeToSdcard(Storage storage, String content, String fileName) {
        File file = new File(storage.getExternalDir(), fileName);
        return FileUtils.writeString(file, content);
    }

    public static void showNoSdcardModal(AppCompatActivity activity) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.hint);
        binding.subTitle.setText(R.string.insert_sdcard_hint);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    public static void exportSuccess(AppCompatActivity activity, Runnable runnable) {
        ExportToSdcardDialog dialog = new ExportToSdcardDialog();
        dialog.show(activity.getSupportFragmentManager(), "");
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            if (runnable != null) {
                runnable.run();
            }
        }, 1000);
    }

    public static JSONObject adapt(ElectrumTx tx) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        adaptInputs(tx, inputs);
        adaptOutputs(tx, outputs);
        object.put("inputs", inputs);
        object.put("outputs", outputs);
        object.put("locktime", tx.getLockTime());
        object.put("version", tx.getVersion());
        return object;
    }

    private static void adaptInputs(ElectrumTx tx, JSONArray inputs) throws JSONException {
        for (TransactionInput transactionInput : tx.getInputs()) {
            JSONObject in = new JSONObject();
            JSONObject utxo = new JSONObject();
            in.put("hash", transactionInput.preTxId);
            in.put("index", transactionInput.preTxIndex);
            in.put("sequence", transactionInput.sequence);
            utxo.put("publicKey", transactionInput.pubKey.pubkey);
            utxo.put("value", transactionInput.value.intValue());
            in.put("utxo", utxo);
            in.put("hash", transactionInput.preTxId);
            in.put("ownerKeyPath", transactionInput.pubKey.hdPath);
            inputs.put(in);

        }
    }

    private static void adaptOutputs(ElectrumTx tx, JSONArray outputs) throws JSONException {
        for (TransactionOutput transactionOutput : tx.getOutputs()) {
            JSONObject out = new JSONObject();
            out.put("address", transactionOutput.address);
            out.put("value", transactionOutput.value);
            outputs.put(out);
        }
    }

    public String getXpub() {
        return xpub;
    }

    public LiveData<String> getMasterPublicKey() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            ExpubInfo expubInfo = new ExpubInfo().invoke();
            String hdPath = expubInfo.getHdPath();
            String expub = expubInfo.getExpub();
            xpub = expub;
            try {
                Account account = Account.parseAccount(hdPath);
                if (account.getParent().getParent().getValue() == 49 && expub.startsWith("xpub")) {
                    exPub.postValue(Util.convertXpubToYpub(expub));
                } else if (expub.startsWith("ypub")) {
                    exPub.postValue(expub);
                }
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }

        });
        return exPub;
    }

    private boolean isSignedTxn(String fileName) {
        Matcher matcher = signedTxnPattern.matcher(fileName);
        return matcher.matches();
    }

    public LiveData<List<String>> loadUnsignTxn() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> fileList = new ArrayList<>();
            if (storage != null) {
                File[] files = storage.getExternalDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".txn")
                                && !isSignedTxn(f.getName())) {
                            fileList.add(f.getName());
                        }
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }

    public LiveData<String> parseTxnFile(String file) {
        MutableLiveData<String> txnHex = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            String content = FileUtils.readString(new File(storage.getExternalDir(), file));
            try {
                JSONObject object = new JSONObject(content);
                String hex = object.getString("hex");
                txnHex.postValue(hex);
            } catch (JSONException e) {
                e.printStackTrace();
                txnHex.postValue(null);
            }
        });
        return txnHex;
    }

    private class ExpubInfo {
        private String hdPath;
        private String expub;

        public String getHdPath() {
            return hdPath;
        }

        public String getExpub() {
            return expub;
        }

        public ExpubInfo invoke() {
            CoinEntity btc = mRepo.loadCoinSync(Coins.coinIdFromCoinCode(Coins.BTC.coinCode()));
            AccountEntity accountEntity = mRepo.loadAccountsForCoin(btc).get(0);
            hdPath = accountEntity.getHdPath();
            expub = accountEntity.getExPub();
            return this;
        }
    }
}
