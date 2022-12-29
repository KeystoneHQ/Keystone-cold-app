package com.keystone.cold.ui.fragment.main.arweave;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSignResultUrBinding;
import com.keystone.cold.databinding.FragmentSolSignResultQrBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.solana.model.SolTxData;
import com.sparrowwallet.hummingbird.UR;

public class URFragment extends BaseFragment<FragmentSignResultUrBinding> {

    private final UR ur;

    public URFragment(UR ur) {
        this.ur = ur;
    }

    static Fragment newInstance(UR ur) {
        return new URFragment(ur);
    }

    @Override
    protected int setView() {
        return R.layout.fragment_sign_result_ur;
    }

    @Override
    protected void init(View view) {
        mBinding.qrcode.qrcode.displayUR(ur);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
