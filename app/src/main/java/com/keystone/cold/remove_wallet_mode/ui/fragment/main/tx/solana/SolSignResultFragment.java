package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolSignResultQrBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import org.json.JSONObject;

public class SolSignResultFragment extends BaseFragment<FragmentSolSignResultQrBinding> {

    private LiveData<JSONObject> parsedTx;


    static Fragment newInstance(@NonNull Bundle bundle, LiveData<JSONObject> parsedTx) {
        SolSignResultFragment fragment = new SolSignResultFragment();
        fragment.setArguments(bundle);
        fragment.parsedTx = parsedTx;
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sol_sign_result_qr;
    }

    @Override
    protected void init(View view) {
        parsedTx.observe(this, jsonObject -> {
            String ur = jsonObject.optString("signatureUR");
            if (!TextUtils.isEmpty(ur)) {
                mBinding.qrcode.qrcode.setData(ur);
            }
            parsedTx.removeObservers(this);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
