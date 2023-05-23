package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.sui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSuiTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.sui.model.SuiTx;

import java.math.BigDecimal;

public class SuiFormattedTxFragment extends BaseFragment<FragmentSuiTxBinding> {

    private LiveData<SuiTx> suiTxLiveData;


    public static Fragment newInstance(@NonNull Bundle bundle, LiveData<SuiTx> suiTxLiveData) {
        SuiFormattedTxFragment fragment = new SuiFormattedTxFragment();
        fragment.setArguments(bundle);
        fragment.suiTxLiveData = suiTxLiveData;
        return fragment;
    }


    @Override
    protected int setView() {
        return R.layout.fragment_sui_tx;
    }

    @Override
    protected void init(View view) {
        if (suiTxLiveData != null) {
            suiTxLiveData.observe(this, suiTx -> {
                if (suiTx != null) {
                    updateUI(suiTx);
                }
            });
        }
    }

    private void updateUI(SuiTx suiTx) {
        mBinding.setCoinCode(Coins.SUI.coinCode());
        mBinding.setCheckInfoTitle(Coins.SUI.coinName());
//        mBinding.tvSender.setText(suiTx.getSender());
//        mBinding.tvExpirationTimeStamp.setText(String.valueOf(suiTx.getExpirationTimestampSecs()));
//        mBinding.tvMaxGasLimit.setText(suiTx.getMaxGasAmount() + " Gas Units");
//        mBinding.tvGasUnitPrice.setText(conversionUnit(String.valueOf(suiTx.getGasUnitPrice())) + " SUI");
//        mBinding.tvChainId.setText(String.valueOf(suiTx.getChainId()));
//
//        if (suiTx instanceof SuiTx) {
//            mBinding.payload.setVisibility(View.GONE);
//            mBinding.llMethod.setVisibility(View.VISIBLE);
//            mBinding.tvMethod.setText(suiTx.getPayLoad().getType());
//            mBinding.llReceiver.setVisibility(View.VISIBLE);
//            mBinding.tvReceiver.setText("Developing");
//            mBinding.llAmount.setVisibility(View.VISIBLE);
//            mBinding.tvAmount.setText("Developing");
//        } else {
//            mBinding.llMethod.setVisibility(View.GONE);
//            mBinding.llAmount.setVisibility(View.GONE);
//            mBinding.llReceiver.setVisibility(View.GONE);
//            mBinding.payload.setVisibility(View.VISIBLE);
//            mBinding.payload.setData(suiTx.getPayLoad());
//        }
        if (!TextUtils.isEmpty(suiTx.getSignatureUR())) {
            mBinding.checkInfoLayout.llHint.setVisibility(View.GONE);
            mBinding.qr.setVisibility(View.VISIBLE);
            mBinding.qrcode.qrcode.setData(suiTx.getSignatureUR());
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }


    @Override
    public void onDestroyView() {
        if (suiTxLiveData != null) {
            suiTxLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    private final static String SUI_UNIT = "100000000";

    private String conversionUnit(String original) {
        try {
            BigDecimal Octa = new BigDecimal(original);
            BigDecimal unit = new BigDecimal(SUI_UNIT);
            BigDecimal APT = Octa.divide(unit);
            return APT.toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }
}
