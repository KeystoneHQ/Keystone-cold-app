package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SubstrateTxViewModel;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.PolkadotViewModel;

public class SubstrateConfirmTransactionFragment extends ConfirmTransactionFragment<SubstrateTransaction, SubstrateTxViewModel> {
    PolkadotViewModel polkadotViewModel;

    @Override
    protected void initViewModel() {
        polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
        viewModel = ViewModelProviders.of(this).get(SubstrateTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[1];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), SubstrateTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {
        Bundle bundle = requireArguments();
        String rawTx = bundle.getString(BundleKeys.SIGN_DATA_KEY);
        polkadotViewModel.parseTransactionAsync(rawTx).observe(this, (v) -> {
            if (v == null) return;
            bundle.putString(BundleKeys.PARSED_TRANSACTION_KEY, v.toString());
            viewModel.parseTxData(bundle);
        });

        viewModel.getObservableTransaction().observe(this, (v) -> {
            try {
                if (v == null) return;
                int checksum = v.getChecksum();
                switch (v.getType()) {
                    case "Sign": {
                        mBinding.sign.setText(R.string.sign);
                        String signContent = polkadotViewModel.getSignContent(checksum).getString("value");
                        mBinding.sign.setOnClickListener((_v) -> handleSignContent(signContent));
                        break;
                    }
                    case "Stub": {
                        mBinding.sign.setText(R.string.approve);
                        mBinding.sign.setOnClickListener(_v -> {
                            try {
                                polkadotViewModel.handleStub(checksum);
                                navigateUp();
                            } catch (PolkadotViewModel.PolkadotException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                handleParseException(new InvalidTransactionException(getString(R.string.incorrect_tx_data), "invalid transaction"));
            }
        });
    }

    @Override
    protected void handleSign() {
        super.handleSign();
    }

    private void handleSignContent(String signContent) {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignContent(signContent);
                    subscribeSignState();
                }, forgetPassword);
    }

    @Override
    protected void onSignSuccess() {
        // for substrate, it is not an UR;
        String signedHex = viewModel.getSignedHex();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_HEX_KEY, signedHex);
        data.putString(BundleKeys.COIN_CODE_KEY, viewModel.getCoinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
