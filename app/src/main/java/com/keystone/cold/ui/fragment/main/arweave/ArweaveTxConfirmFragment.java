package com.keystone.cold.ui.fragment.main.arweave;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArTxConfirmBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class ArweaveTxConfirmFragment extends BaseFragment<ArTxConfirmBinding> {
    @Override
    protected int setView() {
        return R.layout.ar_tx_confirm;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
