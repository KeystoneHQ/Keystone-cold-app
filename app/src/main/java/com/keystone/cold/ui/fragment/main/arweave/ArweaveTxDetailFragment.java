package com.keystone.cold.ui.fragment.main.arweave;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveTxDetailBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class ArweaveTxDetailFragment extends BaseFragment<ArweaveTxDetailBinding> {

    private String tx;

    @Override
    protected int setView() {
        return R.layout.arweave_tx_detail;
    }

    public ArweaveTxDetailFragment(String tx) {
        this.tx = tx;
    }

    @Override
    protected void init(View view) {
        mBinding.txDetail.setText(tx);
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public static ArweaveTxDetailFragment newInstance(String tx) {
        return new ArweaveTxDetailFragment(tx);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
