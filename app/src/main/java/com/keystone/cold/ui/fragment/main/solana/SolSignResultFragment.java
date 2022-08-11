package com.keystone.cold.ui.fragment.main.solana;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolSignResultQrBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.solana.model.SolTxData;

public class SolSignResultFragment extends BaseFragment<FragmentSolSignResultQrBinding> {

    private final SolTxData solTxData;

    public SolSignResultFragment(SolTxData solTxData) {
        this.solTxData = solTxData;
    }

    static Fragment newInstance(SolTxData solTxData) {
        return new SolSignResultFragment(solTxData);
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sol_sign_result_qr;
    }

    @Override
    protected void init(View view) {
        mBinding.qrcode.qrcode.setData(solTxData.getSignatureUR());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
