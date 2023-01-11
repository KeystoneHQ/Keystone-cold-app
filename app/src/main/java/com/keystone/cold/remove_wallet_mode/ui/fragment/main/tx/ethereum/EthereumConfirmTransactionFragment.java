package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.views.AuthenticateModal;

public class EthereumConfirmTransactionFragment extends ConfirmTransactionFragment<EthereumTxViewModel> {
    private MutableLiveData<EthereumTransaction> transaction;

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(EthereumTxViewModel.class);
        transaction = viewModel.getObservableEthTx();
        viewModel.generateUnsignedTransaction(requireArguments());
        viewModel.parseTxException().observe(this, this::handleParseException);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[2];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), EthereumTransactionDetailFragment.newInstance(requireArguments(), transaction));
        layoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {
        mBinding.toolbar.setNavigationOnClickListener((v) -> navigateUp());
        mBinding.sign.setOnClickListener((v) -> checkExceedFeeDialog());
    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSign();
                    subscribeSignState();
                }, forgetPassword);
    }

    private void handleParseException(BaseException ex) {
        if (ex != null) {
            ex.printStackTrace();
            alertException(ex, () -> {
                popBackStack(R.id.assetFragment, false);
            });
            viewModel.parseTxException().setValue(null);
        }
    }

    private void checkExceedFeeDialog() {
        if (viewModel.isExceeded()) {
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
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
