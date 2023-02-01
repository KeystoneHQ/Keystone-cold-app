package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.substrate;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.RawTxFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.SubstrateTxViewModel;
import com.keystone.cold.ui.modal.PolkadotErrorDialog;
import com.keystone.cold.viewmodel.PolkadotViewModel;

import org.json.JSONObject;

public class SubstrateConfirmTransactionFragment extends ConfirmTransactionFragment<SubstrateTxViewModel> {
    PolkadotViewModel polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SubstrateTxViewModel.class);
        viewModel.reset();
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[2];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), SubstrateTransactionDetailFragment.newInstance(requireArguments(), viewModel.getTransaction()));
        configs[1] = new TabLayoutConfig(getString(R.string.raw_data), RawTxFragment.newInstance(requireArguments(), viewModel.getRawFormatTx()));
        return configs;
    }

    @Override
    protected void setupView() {
        try {
            Bundle bundle = requireArguments();
            String rawTx = bundle.getString(BundleKeys.SIGN_DATA_KEY);
            JSONObject parsedMessage = polkadotViewModel.parseTransaction(rawTx);
            bundle.putString(BundleKeys.PARSED_TRANSACTION_KEY, parsedMessage.toString());
            viewModel.parseTxData(bundle);
        } catch (Exception e){
            handleParseException(new InvalidTransactionException(getString(R.string.incorrect_tx_data), "invalid transaction"));
        }

        viewModel.getTransaction().observe(this, (v) -> {
            try {
                if (v == null) return;
                int checksum = v.getChecksum();
                switch (v.getType()) {
                    case "Sign": {
                        mBinding.sign.setText(R.string.sign);
                        String signContent = polkadotViewModel.getSignContent(checksum).getString("value");
                        mBinding.sign.setOnClickListener((_v) -> viewModel.handleSignContent(signContent));
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
        viewModel.getException().observe(this, (e) -> {
            if (e == null) return;
            handleParseException(e);
        });
    }

    private void handleParseException(BaseException ex) {
        if (ex != null) {
            ex.printStackTrace();
            alertException(ex, () -> {
                popBackStack(R.id.myAssetsFragment, false);
            });
            viewModel.getException().setValue(null);
        }
    }

    @Override
    protected void handleSign() {
        super.handleSign();
    }

    @Override
    protected void onSignSuccess() {
        // for substrate, it is not an UR;
        String signedHex = viewModel.getSignedHex();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signedHex);
        data.putString(BundleKeys.COIN_CODE_KEY, viewModel.getCoinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
