package com.keystone.cold.ui.fragment.main.psbt;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.PsbtConfirmFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;
import java.util.List;

public class PSBTTransactionFragment extends BaseFragment<PsbtConfirmFragmentBinding> {
    @Override
    protected int setView() {
        return R.layout.psbt_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String txId = data.getString(KEY_TX_ID);
        DataRepository repository = MainApplication.getApplication().getRepository();

        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.txDetail.scanHint.setText(getString(R.string.broadcast_with_core_wallet));
        repository.loadTx(txId).observe(this, v -> {
            try {
                List<JSONObject> inputs = new ArrayList<>();
                List<JSONObject> outputs = new ArrayList<>();
                String fee;

                JSONObject object = new JSONObject(v.getAddition());
                JSONObject parsedPSBT = object.getJSONObject("parsed_message");
                JSONArray pinputs = parsedPSBT.getJSONArray("inputs");
                JSONArray poutputs = parsedPSBT.getJSONArray("outputs");
                fee = parsedPSBT.getString("fee");
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
                mBinding.txDetail.fromList.setAdapter(inputAdapter);

                PSBTOutputAdapter outputAdapter = new PSBTOutputAdapter(mActivity);
                outputAdapter.setItems(outputs);
                mBinding.txDetail.toList.setAdapter(outputAdapter);

                mBinding.txDetail.fee.setText(fee);

                String signedPSBTB64 = v.getSignedHex();

                mBinding.txDetail.dynamicQrcodeLayout.qrcode.displayUR(new CryptoPSBT(Base64.decode(signedPSBTB64)).toUR());
            } catch (JSONException e) {
                e.printStackTrace();
                this.alert("Data error", "this transaction might be invalid", this::navigateUp);
            }
        });

        mBinding.sign.setVisibility(View.GONE);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
