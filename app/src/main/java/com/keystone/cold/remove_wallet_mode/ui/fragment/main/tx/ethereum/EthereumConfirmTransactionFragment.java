package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.Objects;

public class EthereumConfirmTransactionFragment extends ConfirmTransactionFragment<EthereumTransaction, EthereumTxViewModel> {
    private MutableLiveData<EthereumTransaction> transaction;

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(EthereumTxViewModel.class);
        viewModel.reset();
        transaction = viewModel.getObservableTransaction();
        viewModel.generateUnsignedTransaction(requireArguments());
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[1];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), EthereumTransactionDetailFragment.newInstance(requireArguments(), transaction));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {
        mBinding.toolbar.setNavigationOnClickListener((v) -> navigateUp());
        mBinding.sign.setOnClickListener((v) -> checkExceedFeeDialog());
    }

    private void checkExceedFeeDialog() {
        if (Objects.requireNonNull(transaction.getValue()).isFeeExceeded()) {
            ModalDialog.showTwoButtonCommonModal(mActivity,
                    getString(R.string.atention),
                    getString(R.string.exceed_fee),
                    getString(R.string.sign),
                    getString(R.string.cancel),
                    this::handleSign,
                    null);
            return;
        }
        handleSign();
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.ETH.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
