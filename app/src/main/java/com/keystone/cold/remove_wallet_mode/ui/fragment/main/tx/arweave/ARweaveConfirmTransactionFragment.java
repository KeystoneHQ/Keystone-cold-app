package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_AUTH_RESULT_KEY;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_REJECTED;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_STATUS_KEY;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_SUCCESS;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.model.ArweaveTransaction;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.ARPubkeyNotFoundException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.ARweaveTxViewModel;
import com.keystone.cold.ui.modal.ModalDialog;

public class ARweaveConfirmTransactionFragment extends ConfirmTransactionFragment<ArweaveTransaction, ARweaveTxViewModel> {
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ARweaveTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] layoutConfigs = new TabLayoutConfig[1];
        layoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), ARweaveTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return layoutConfigs;
    }

    @Override
    protected void setupView() {
        viewModel.parseTxData(requireArguments());
        mBinding.sign.setOnClickListener((v) -> handleSign());
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.AR.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }

    @Override
    protected void handleParseException(BaseException ex) {
        if (ex instanceof ARPubkeyNotFoundException) {
            ModalDialog.showRemindModal(mActivity, getString(R.string.arweave_authenticate_hint), getString(R.string.add), () -> {
                Bundle data = new Bundle();
                data.putBoolean(BundleKeys.AR_AUTH_FROM_SIGN_KEY, true);
                navigate(R.id.action_to_ArweaveAuthFragment, data);
                FragmentManager fragmentManager = this.getParentFragmentManager();
                fragmentManager.setFragmentResultListener(AR_AUTH_RESULT_KEY, this, (s, bundle) -> {
                    String result = bundle.getString(AR_SETUP_STATUS_KEY);
                    switch (result) {
                        case AR_SETUP_SUCCESS:
                            viewModel.parseTxData(requireArguments());
                            fragmentManager.clearFragmentResultListener(AR_AUTH_RESULT_KEY);
                            break;
                        case AR_SETUP_REJECTED:
                            fragmentManager.clearFragmentResultListener(AR_AUTH_RESULT_KEY);
                            this.navigateUp();
                            break;
                    }
                });
            }, this::navigateUp);
            viewModel.getObservableException().setValue(null);
        } else {
            super.handleParseException(ex);
        }
    }
}
