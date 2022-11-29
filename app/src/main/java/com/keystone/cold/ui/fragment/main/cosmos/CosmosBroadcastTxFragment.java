package com.keystone.cold.ui.fragment.main.cosmos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.AssetListFragment;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;
import com.keystone.cold.util.StepFragmentHelper;
import com.keystone.cold.viewmodel.WatchWallet;

public class CosmosBroadcastTxFragment extends BroadcastTxFragment {

    private String signatureURString;

    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }


    @Override
    protected void init(View view) {

        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.complete.setOnClickListener(goHome);
        Bundle data = requireArguments();
        signatureURString = data.getString(KEY_SIGNATURE_UR);
        mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());
        String coinCode = data.getString(KEY_COIN_CODE);
        if (!TextUtils.isEmpty(coinCode)) {
            mBinding.setCoinCode(coinCode);
        } else {
            mBinding.setCoinCode("cosmos_default");
        }
        String des = StepFragmentHelper.getInstance().getStartingPoint();
        if (!TextUtils.isEmpty(des) && des.equals(AssetListFragment.class.getName())) {
            mBinding.toolbar.setNavigationOnClickListener(goHome);
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        }
        mBinding.broadcastHint.setText(R.string.sync_with_metamask);
    }


    @Override
    public String getSignedTxData() {
        return signatureURString;
    }
}
