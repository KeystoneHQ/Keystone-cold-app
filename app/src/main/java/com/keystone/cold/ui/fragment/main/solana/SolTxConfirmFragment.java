package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.SOL.SolImpl;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.SolTxConfirmBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

public class SolTxConfirmFragment extends BaseFragment<SolTxConfirmBinding> {
    private SolTxViewModel viewModel;
    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
    @Override
    protected int setView() {
        return R.layout.sol_tx_confirm;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(this).get(SolTxViewModel.class);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> {
            handleSign();
        });
        viewModel.parseTxData(requireArguments());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSign();
                }, forgetPassword);
    }
}
