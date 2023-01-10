package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentAptosTxBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTransferTx;
import com.keystone.cold.ui.fragment.main.aptos.model.AptosTx;

import java.math.BigDecimal;

public class AptosFormattedTxFragment extends BaseFragment<FragmentAptosTxBinding> {

    private AptosTxViewModel viewModel;
    private boolean isFromRecord = false;


    public static Fragment newInstance(@NonNull Bundle bundle) {
        AptosFormattedTxFragment fragment = new AptosFormattedTxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(@NonNull Bundle bundle, boolean isFromRecord) {
        AptosFormattedTxFragment fragment = new AptosFormattedTxFragment();
        fragment.isFromRecord = isFromRecord;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_aptos_tx;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(getParentFragment()).get(AptosTxViewModel.class);
        viewModel.getAptosTxLiveData().observe(this, aptosTx -> {
            if (aptosTx != null) {
                updateUI(aptosTx);
            }
        });
    }

    private void updateUI(AptosTx aptosTx) {
        mBinding.tvSender.setText(aptosTx.getSender());
        mBinding.tvSequenceNo.setText(String.valueOf(aptosTx.getSequenceNumber()));
        mBinding.tvExpirationTimeStamp.setText(String.valueOf(aptosTx.getExpirationTimestampSecs()));
        mBinding.tvMaxGasLimit.setText(aptosTx.getMaxGasAmount() + " Gas Units");
        mBinding.tvGasUnitPrice.setText(conversionUnit(String.valueOf(aptosTx.getGasUnitPrice())) + " APT");
        mBinding.tvChainId.setText(String.valueOf(aptosTx.getChainId()));

        if (aptosTx instanceof AptosTransferTx) {
            mBinding.payload.setVisibility(View.GONE);
            mBinding.llMethod.setVisibility(View.VISIBLE);
            mBinding.tvMethod.setText(getString(R.string.transfer));
            mBinding.llReceiver.setVisibility(View.VISIBLE);
            mBinding.tvReceiver.setText(((AptosTransferTx) aptosTx).getReceiver());
            mBinding.llAmount.setVisibility(View.VISIBLE);
            mBinding.tvAmount.setText(conversionUnit(((AptosTransferTx) aptosTx).getAmount()) + " APT");
        } else {
            mBinding.llMethod.setVisibility(View.GONE);
            mBinding.llAmount.setVisibility(View.GONE);
            mBinding.llReceiver.setVisibility(View.GONE);
            mBinding.payload.setVisibility(View.VISIBLE);
            mBinding.payload.setData(aptosTx.getPayLoad());
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private final static String APT_UNIT = "100000000";

    private String conversionUnit(String original) {
        try {
            BigDecimal Octa = new BigDecimal(original);
            BigDecimal unit = new BigDecimal(APT_UNIT);
            BigDecimal APT = Octa.divide(unit);
            return APT.toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }
}
