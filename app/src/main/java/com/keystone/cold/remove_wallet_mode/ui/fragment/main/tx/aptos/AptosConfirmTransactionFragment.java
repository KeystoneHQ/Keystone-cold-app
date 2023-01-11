package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.aptos.AptosFormattedTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.AptosTxViewModel;
import com.keystone.cold.ui.views.AuthenticateModal;

public class AptosConfirmTransactionFragment extends ConfirmTransactionFragment<AptosTxViewModel> {


    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AptosTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] tabLayoutConfigs = new TabLayoutConfig[2];
        tabLayoutConfigs[0] = new TabLayoutConfig(getString(R.string.overview), AptosFormattedTxFragment.newInstance(requireArguments(), viewModel.getAptosTxLiveData()));
        tabLayoutConfigs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return tabLayoutConfigs;
    }

    @Override
    protected void setupView() {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> {
            handleSign();
        });
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, Coins.APTOS.coinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        viewModel.parseTxData(requireArguments());
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
}
