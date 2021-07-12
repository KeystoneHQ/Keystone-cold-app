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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.exception.CoinNotFindException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.protocol.ZipUtil;
import com.keystone.cold.protocol.parser.ProtoParser;
import com.keystone.cold.ui.fragment.main.QRCodeScanFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;

public class QrScanViewModel extends AndroidViewModel {

    private static final String TAG = "Vault.Qrcode.QrScanViewModel";

    private final boolean isSetupVault;
    private QRCodeScanFragment fragment;

    private QrScanViewModel(@NonNull Application application, boolean isSetupVault) {
        super(application);
        this.isSetupVault = isSetupVault;
    }

    public void handleUrQrCode(QRCodeScanFragment owner, String hex) throws
            UnknowQrCodeException, XfpNotMatchException,
            InvalidTransactionException, JSONException, CoinNotFindException {
        this.fragment = owner;

        //xrp toolkit tx, metamask tx, webauth tx, decode as JSON
        JSONObject object = tryDecodeAsJson(hex);

        //keystone, try decode as protobuf
        if (object == null) {
            object = tryDecodeAsProtobuf(hex);
        }

        if (object != null) {
            decodeAndProcess(object);
        } else {
            throw new UnknowQrCodeException("unknown UR qr code");
        }
    }

    private JSONObject tryDecodeAsProtobuf(String hex) {
        JSONObject object;
        hex = ZipUtil.unzip(hex);
        object = new ProtoParser(Hex.decode(hex)).parseToJson();
        return object;
    }

    private JSONObject tryDecodeAsJson(String hex) {
        try {
            return new JSONObject(new String(Hex.decode(hex)));
        } catch (Exception ignored) {
        }
        return null;
    }

    private void decodeAndProcess(JSONObject object)
            throws InvalidTransactionException,
            CoinNotFindException,
            JSONException,
            XfpNotMatchException {
        logObject(object);

        //should handle web Auth qr code under any watch wallet mode
        if (checkWebAuth(object)) return;
        switch (WatchWallet.getWatchWallet(getApplication())) {
            case XRP_TOOLKIT:
                if (object.has("TransactionType")) {
                    handleSignXrpTx(object);
                    return;
                }
                throw new InvalidTransactionException("unknown qr code type");
            case KEYSTONE:
                if (object.optString("type").equals("TYPE_SIGN_TX")) {
                    handleSign(object);
                    return;
                }
                throw new InvalidTransactionException("unknown qr code type");
            case METAMASK:
                String txHex = object.optString("txHex");
                JSONObject data = object.optJSONObject("data");
                AppExecutors.getInstance().diskIO().execute(() -> {
                    try {
                        if (!TextUtils.isEmpty(txHex) && EthImpl.decodeRawTransaction(txHex, null) != null) {
                            handleSignMetamaskTx(object);
                        } else if (data != null) {
                            handleSignMetamaskMessage(object);
                        } else {
                            throw new InvalidTransactionException("unknown qr code type");
                        }
                    } catch (XfpNotMatchException | InvalidTransactionException e) {
                        AppExecutors.getInstance().mainThread().execute(() -> fragment.handleException(e));
                    }
                });
        }
    }

    private boolean checkWebAuth(JSONObject object) throws JSONException {
        JSONObject webAuth = object.optJSONObject("data");
        if (webAuth != null && webAuth.optString("type").equals("webAuth")) {
            handleWebAuth(webAuth);
            return true;
        }
        return false;
    }

    private void handleSignMetamaskTx(JSONObject object) throws XfpNotMatchException {
        if (!object.optString("xfp").equalsIgnoreCase(new GetMasterFingerprintCallable().call())) {
            throw new XfpNotMatchException("xfp not match");
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_DATA, object.toString());
        AppExecutors.getInstance().mainThread().execute(() -> fragment.navigate(R.id.action_to_ethTxConfirmFragment, bundle));
    }

    private void handleSignMetamaskMessage(JSONObject object) throws XfpNotMatchException {
        if (!object.optString("xfp").equalsIgnoreCase(new GetMasterFingerprintCallable().call())) {
            throw new XfpNotMatchException("xfp not match");
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_DATA, object.toString());
        AppExecutors.getInstance().mainThread().execute(() -> fragment.navigate(R.id.action_to_ethSignMessageFragment, bundle));
    }

    private void handleSignXrpTx(JSONObject object) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_DATA, object.toString());
        fragment.navigate(R.id.action_to_xrpTxConfirmFragment, bundle);
    }

    private void logObject(JSONObject object) {
        try {
            Log.w(TAG, "object = " + object.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleWebAuth(JSONObject object) throws JSONException {
        String data = object.getString("data");
        Bundle bundle = new Bundle();
        bundle.putString(WEB_AUTH_DATA, data);
        bundle.putBoolean(IS_SETUP_VAULT, isSetupVault);
        if (isSetupVault) {
            fragment.navigate(R.id.action_to_webAuthResultFragment, bundle);
        } else {
            fragment.navigate(R.id.action_QRCodeScan_to_result, bundle);
        }
    }

    private void handleSign(JSONObject object)
            throws InvalidTransactionException,
            CoinNotFindException,
            XfpNotMatchException {
        checkXfp(object);
        try {
            JSONObject transaction = object.getJSONObject("signTx");
            String coinCode = transaction.getString("coinCode");
            if (!WatchWallet.isSupported(getApplication(), coinCode) || transaction.has("omniTx")) {
                throw new CoinNotFindException("not support " + coinCode);
            }
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TX_DATA, object.getJSONObject("signTx").toString());
            fragment.navigate(R.id.action_to_txConfirmFragment, bundle);
        } catch (JSONException e) {
            throw new InvalidTransactionException("invalid transaction");
        }
    }

    private void checkXfp(JSONObject obj) throws XfpNotMatchException {
        String xfp = new GetMasterFingerprintCallable().call();
        if (!obj.optString("xfp").equals(xfp)) {
            throw new XfpNotMatchException("xfp not match");
        }
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;

        private final boolean mIsSetupVault;

        public Factory(@NonNull Application application, boolean isSetupVault) {
            mApplication = application;
            mIsSetupVault = isSetupVault;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new QrScanViewModel(mApplication, mIsSetupVault);
        }
    }
}
