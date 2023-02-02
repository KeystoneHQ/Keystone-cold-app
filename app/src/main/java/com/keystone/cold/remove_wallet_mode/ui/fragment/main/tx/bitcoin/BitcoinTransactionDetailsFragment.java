package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.bitcoin;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentBitcoinTxBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BitcoinTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BitcoinTransactionDetailsFragment extends BaseFragment<FragmentBitcoinTxBinding> {
    private MutableLiveData<PSBT> psbt;

    public BitcoinTransactionDetailsFragment(MutableLiveData<PSBT> psbt) {
        this.psbt = psbt;
    }

    public static BitcoinTransactionDetailsFragment newInstance(Bundle bundle, MutableLiveData<PSBT> psbt) {
        BitcoinTransactionDetailsFragment fragment = new BitcoinTransactionDetailsFragment(psbt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_bitcoin_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.contentContainer.setVisibility(View.GONE);
        psbt.observe(this, v -> {
            if (v == null) return;
            try {
                mBinding.contentContainer.setVisibility(View.VISIBLE);
                String coinCode = BitcoinTxViewModel.getCoinCodeFromPSBT(v);
                mBinding.setCoinCode(coinCode);
                mBinding.setCheckInfoTitle(Coins.coinNameFromCoinCode(coinCode));
                JSONObject parsedPSBT;
                List<JSONObject> inputs = new ArrayList<>();
                List<JSONObject> outputs = new ArrayList<>();
                parsedPSBT = v.generateParsedMessage();
                JSONArray pinputs = parsedPSBT.getJSONArray("inputs");
                JSONArray poutputs = parsedPSBT.getJSONArray("outputs");
                String fee = parsedPSBT.getString("fee");
                int length = pinputs.length();
                for (int i = 0; i < length; i++) {
                    JSONObject o = pinputs.getJSONObject(i);
                    inputs.add(o);
                }

                length = poutputs.length();
                for (int i = 0; i < length; i++) {
                    JSONObject o = poutputs.getJSONObject(i);
                    outputs.add(o);
                }

                PSBTInputAdapter inputAdapter = new PSBTInputAdapter(mActivity);

                inputAdapter.setItems(inputs);
                mBinding.fromList.setAdapter(inputAdapter);

                PSBTOutputAdapter outputAdapter = new PSBTOutputAdapter(mActivity);
                outputAdapter.setItems(outputs);
                mBinding.toList.setAdapter(outputAdapter);

                mBinding.fee.setText(fee);

                if (v.getSignedBase64() != null){
                    mBinding.qr.setVisibility(View.VISIBLE);
                    mBinding.qrcode.qrcode.setData(v.getSignatureQRCode());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
