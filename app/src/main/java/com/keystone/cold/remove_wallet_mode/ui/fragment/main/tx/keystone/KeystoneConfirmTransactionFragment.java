package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.keystone;

import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ProgressModalBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ConfirmTransactionFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.KeystoneTxViewModel;
import com.keystone.cold.ui.modal.ModalDialog;

public class KeystoneConfirmTransactionFragment extends ConfirmTransactionFragment<TxEntity, KeystoneTxViewModel> {
    private ModalDialog addingAddressDialog;
    private int feeAttackCheckingState;
    private FeeAttackChecking feeAttackChecking;

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(KeystoneTxViewModel.class);
    }

    @Override
    protected TabLayoutConfig[] getTabLayouts() {
        TabLayoutConfig[] configs = new TabLayoutConfig[1];
        configs[0] = new TabLayoutConfig(getString(R.string.overview), KeystoneTransactionDetailFragment.newInstance(requireArguments(), viewModel.getObservableTransaction()));
        return configs;
    }

    @Override
    protected void setupView() {
        viewModel.parseTxData(requireArguments());
        viewModel.getAddingAddressState().observe(this, b -> {
            if (b) {
                addingAddressDialog = ModalDialog.newInstance();
                ProgressModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.progress_modal, null, false);
                binding.text.setText(R.string.sync_in_progress);
                binding.text.setVisibility(View.VISIBLE);
                addingAddressDialog.setBinding(binding);
                addingAddressDialog.show(mActivity.getSupportFragmentManager(), "");
            } else {
                if (addingAddressDialog != null) {
                    addingAddressDialog.dismiss();
                }
            }
        });
        viewModel.feeAttackChecking().observe(this, state -> {
            feeAttackCheckingState = state;
            if (state != NORMAL) {
                feeAttackChecking = new FeeAttackChecking(this);
            }
        });
        mBinding.sign.setOnClickListener((v) -> handleSign());
    }

    @Override
    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, viewModel.getCoinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }

    @Override
    protected void handleSign() {
        if (feeAttackCheckingState == SAME_OUTPUTS) {
            feeAttackChecking.showFeeAttackWarning();
            return;
        }
        super.handleSign();
    }
}
