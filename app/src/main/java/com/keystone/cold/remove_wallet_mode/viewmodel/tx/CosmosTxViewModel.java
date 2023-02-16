package com.keystone.cold.remove_wallet_mode.viewmodel.tx;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.cryptocore.CosmosParser;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.CosmosTx;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class CosmosTxViewModel extends BaseTxViewModel {

    private String txHex;
    private String messageData;
    private String hdPath;
    private String dataType;

    private String requestId;

    private String xPub;
    private String chainId;
    private String parseJson;


    private final MutableLiveData<CosmosTx> cosmosTxLiveData;


    public CosmosTxViewModel(@NonNull Application application) {
        super(application);
        cosmosTxLiveData = new MutableLiveData<>();
    }


    public LiveData<CosmosTx> getCosmosTxLiveData() {
        return cosmosTxLiveData;
    }

    @Override
    public void parseTxData(Bundle bundle) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            txHex = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            hdPath = bundle.getString(BundleKeys.HD_PATH_KEY);
            requestId = bundle.getString(BundleKeys.REQUEST_ID_KEY);
            dataType = bundle.getString(BundleKeys.DATA_TYPE_KEY);
            try {
                if ("sign-type-amino".equals(dataType)) {
                    parseAminoTx();
                } else if ("sign-type-direct".equals(dataType)) {
                    parseDirectTx();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            xPub = getXpubByPath(hdPath);
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


    public String getCosmosCoinCode() {
        return Coins.getCosmosCoinCode(chainId);
    }

    private void parseDirectTx() {
        String parseResult = CosmosParser.parse(txHex);
        if (parseResult != null) {
            parseJson = CosmosTx.transformDirectToAmino(parseResult);
            CosmosTx cosmosTx = CosmosTx.from(parseJson);
            cosmosTxLiveData.postValue(cosmosTx);
            if (cosmosTx != null) {
                chainId = cosmosTx.getChainId();
            }
            try {
                rawFormatTx.postValue(new JSONObject(parseJson).toString(2));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void parseAminoTx() {
        parseJson = new String(Hex.decode(txHex));
        CosmosTx cosmosTx = CosmosTx.from(parseJson);
        cosmosTxLiveData.postValue(cosmosTx);
        if (cosmosTx != null) {
            chainId = cosmosTx.getChainId();
        }
        try {
            rawFormatTx.postValue(new JSONObject(parseJson).toString(2));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    private String getXpubByPath(String path) {
        AddressEntity addressEntity;
        if (isEvmosPath(path)) {
            addressEntity = repository.loadAddressByPathAndCoinId(path, Coins.EVMOS.coinId());
        } else {
            addressEntity = repository.loadAddressBypath(path);
        }
        if (addressEntity != null) {
            String addition = addressEntity.getAddition();
            try {
                JSONObject rootJson = new JSONObject(addition);
                JSONObject additionJson = rootJson.getJSONObject("addition");
                return additionJson.getString("xPub");
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    private boolean isEvmosPath(String path) {
        //M/44'/60'/0'/0/0
        if (null != path) {
            String[] segs = path.split("/");
            String coinType = segs[2].replace("'", "");
            return coinType.equals(String.valueOf(Coins.EVMOS.coinIndex()));
        }
        return false;
    }
}
