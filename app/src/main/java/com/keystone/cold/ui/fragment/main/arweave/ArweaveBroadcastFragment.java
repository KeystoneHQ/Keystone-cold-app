package com.keystone.cold.ui.fragment.main.arweave;

import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;

public class ArweaveBroadcastFragment extends BroadcastTxFragment {

    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }

    private String ur;

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        mBinding.setCoinCode(Coins.AR.coinCode());
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        mBinding.broadcastHint.setText(R.string.sync_with_watch_only);

        ur = data.getString(KEY_SIGNATURE_UR);

        mBinding.qrcodeLayout.qrcode.setData(ur);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
