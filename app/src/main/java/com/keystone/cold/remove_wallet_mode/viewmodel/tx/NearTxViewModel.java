package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.cryptocore.NearParser;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.near.model.NearTx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NearTxViewModel extends BaseTxViewModel {

    private static final String TAG = NearTxViewModel.class.getSimpleName();

    private String hdPath;
    private String requestId;
    private List<String> txHexList;

    private List<String> signatureList = new ArrayList<>();
    private List<String> txHashList = new ArrayList<>();
    private List<String> formattedJsonList = new ArrayList<>();

    private int transactionNum = 0;

    private final MutableLiveData<NearTx> nearTxLiveData;

    public NearTxViewModel(@NonNull Application application) {
        super(application);
        nearTxLiveData = new MutableLiveData<>();
    }

    public LiveData<NearTx> getNearTxLiveData() {
        return nearTxLiveData;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHashList.clear();
            formattedJsonList.clear();
            signatureList.clear();
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            txHexList = (List<String>) bundle.getSerializable(BundleKeys.SIGN_DATA_KEY);
            if (txHexList == null) {
                rawFormatTx.postValue(null);
                return;
            }
            transactionNum = txHexList.size();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < transactionNum; i++) {

                String parseResult = NearParser.parse(txHexList.get(i));
                if (parseResult != null) {
                    String json = getFormattedJson(parseResult);
                    if (json == null) {
                        Log.e(TAG, "have no formatted data");
                        rawFormatTx.postValue(null);
                        return;
                    }
                    Log.e(TAG, String.format("onSuccess is %s", json));
                    NearTx nearTx = NearTx.from(json);
                    nearTxLiveData.postValue(nearTx);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(json);
                        txHashList.add(jsonObject.getString("hash"));
                        jsonObject.remove("hash");
                        jsonArray.put(jsonObject);
                        formattedJsonList.add(jsonObject.toString());
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                    if (transactionNum - 1 == i) {
                        try {
                            rawFormatTx.postValue(jsonArray.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "parse error");
                    rawFormatTx.postValue(null);
                }
            }

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


    private String getFormattedJson(String nearStr) {
        try {
            JSONObject jsonObject = new JSONObject(nearStr);
            return jsonObject.getJSONObject("formatted_json").toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
