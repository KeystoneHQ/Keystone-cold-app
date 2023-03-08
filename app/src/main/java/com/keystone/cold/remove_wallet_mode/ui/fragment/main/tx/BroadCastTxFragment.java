package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentBroadcastTxBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.ui.fragment.BaseFragment;

public class BroadCastTxFragment extends BaseFragment<FragmentBroadcastTxBinding> {

    @Override
    protected int setView() {
        return R.layout.fragment_broadcast_tx;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String signatureURString = data.getString(BundleKeys.SIGNATURE_UR_KEY);
        String signatureHexString = data.getString(BundleKeys.SIGNATURE_HEX_KEY, "");
        String coinCode = data.getString(BundleKeys.COIN_CODE_KEY);
        if (!signatureHexString.isEmpty()) {
            mBinding.qrcodeLayout.qrcode.disableMultipart();
            mBinding.qrcodeLayout.qrcode.setData(signatureHexString);
        }
        else {
            mBinding.qrcodeLayout.qrcode.setData(signatureURString);
        }
        if (coinCode.startsWith("BTC")) {
            coinCode = Coins.BTC.coinCode();
        }
        mBinding.setCoinCode(coinCode);
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.myAssetsFragment, false));
        mBinding.complete.setOnClickListener(v -> popBackStack(R.id.myAssetsFragment, false));

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
