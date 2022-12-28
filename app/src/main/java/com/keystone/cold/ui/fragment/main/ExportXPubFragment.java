package com.keystone.cold.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;

import android.os.Bundle;
import android.view.View;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.accounts.ExtendedPublicKeyVersion;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.databinding.ExportXpubFragmentBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class ExportXPubFragment extends BaseFragment<ExportXpubFragmentBinding> {
    @Override
    protected int setView() {
        return R.layout.export_xpub_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String coinCode = data.getString(KEY_COIN_CODE);
        String xpub = getXPubByCoin(coinCode);
        mBinding.toolbar.setNavigationOnClickListener((v) -> navigateUp());
        mBinding.setCoinCode(coinCode);
        mBinding.xpub.setText("(" + xpub + ")");
        mBinding.qrcodeLayout.qrcode.setData(xpub);
    }

    private String getXPubByCoin(String coinCode) {
        int purpose = Coins.purposeNumber(coinCode);
        int coinIndex;
        switch (coinCode) {
            case "LTC":
                coinIndex = 2;
                break;
            case "DASH":
                coinIndex = 5;
                break;
            case "BCH":
                coinIndex = 145;
                break;
            default:
                coinIndex = 0;
        }

        String path = "M/" + purpose + "'/" + coinIndex + "'/0'";
        String xpub = new GetExtendedPublicKeyCallable(path).call();
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xpub);
        switch (coinCode) {
            case "BTC":
            case "LTC":
                return extendedPublicKey.toVersion(ExtendedPublicKeyVersion.ypub);
            case "BTC_NATIVE_SEGWIT":
                return extendedPublicKey.toVersion(ExtendedPublicKeyVersion.zpub);
            default:
                return xpub;
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
