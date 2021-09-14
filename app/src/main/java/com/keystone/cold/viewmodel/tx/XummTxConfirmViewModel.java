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

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.coins.XRP.Xrp;
import com.keystone.coinlib.coins.XRP.XrpImpl;
import com.keystone.coinlib.coins.XRP.SupportTransactions;
import com.keystone.coinlib.coins.XRP.XrpTransaction;
import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryption.ChipSigner;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Objects;

public class XummTxConfirmViewModel extends Base {

    private JSONObject xummTxObj;
    private String account;
    private String signingPubKey;
    private String signingKeyPath;
    private String txId;

    private final MutableLiveData<JSONObject> displayJson = new MutableLiveData<>();

    public XummTxConfirmViewModel(@NonNull Application application) {
        super(application);
        coinCode = Coins.XRP.coinCode();
    }

    public MutableLiveData<JSONObject> getDisplayJson() {
        return displayJson;
    }

    public void parseXummTxData(JSONObject object) {
        xummTxObj = object;
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                XrpTransaction xrpTransaction = SupportTransactions.get(object.getString("TransactionType"));

                if (xrpTransaction == null || !xrpTransaction.isValid(object)) {
                    parseTxException.postValue(new InvalidTransactionException("invalid xrp exception"));
                    return;
                }
                account = xummTxObj.optString("Account");
                signingPubKey = xummTxObj.optString("SigningPubKey");
                if (!isValidAccount()) {
                    parseTxException.postValue(new InvalidTransactionException("invalid xrp account"));
                    return;
                }
                if (!checkAccount()) {
                    parseTxException.postValue(new InvalidAccountException("account not match"));
                    return;
                }
                displayJson.postValue(xrpTransaction.flatTransactionDetail(object));
                TxEntity tx = new TxEntity();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                tx.setCoinCode(coinCode);
                tx.setTimeStamp(getUniversalSignIndex(getApplication()));
                tx.setSignId(watchWallet.getSignId());
                tx.setCoinId(Coins.XRP.coinId());
                tx.setBelongTo(mRepository.getBelongTo());
                observableTx.postValue(tx);
            } catch (JSONException e) {
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
        for (AddressEntity address : mRepository.loadAddressSync(Coins.XRP.coinId())) {
            if (address.getAddressString().equals(account)) {
                signingKeyPath = address.getPath().toLowerCase();
                return true;
            }
        }
        return false;
    }

    public void handleSignXummTransaction() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignTxCallback();
            callback.startSign();
            Signer signer = new ChipSigner(signingKeyPath, getAuthToken(), signingPubKey);
            signXummTransaction(xummTxObj, callback, signer);
        });
    }

    protected TxEntity onSignSuccess(String txId, String rawTx) {
        this.txId = txId;
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        try {
            xummTxObj.put("txHex", rawTx);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tx.setSignedHex(xummTxObj.toString());
        mRepository.insertTx(tx);
        return tx;
    }

    @Override
    public String getTxId() {
        return txId;
    }

    public void signXummTransaction(JSONObject txObj, SignCallback callback, Signer signer ) {
        XrpImpl xrp = new XrpImpl();
        xrp.generateJsonTransaction(txObj, callback, signer);
    }
}
