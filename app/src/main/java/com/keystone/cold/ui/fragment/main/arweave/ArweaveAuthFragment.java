package com.keystone.cold.ui.fragment.main.arweave;

import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveAuthFragmentBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.views.AuthenticateModal;


public class ArweaveAuthFragment extends BaseFragment<ArweaveAuthFragmentBinding> {
    @Override
    protected int setView() {
        return R.layout.arweave_auth_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener((v) -> {
            navigateUp();
            navigate(R.id.chooseWatchWalletFragment);
        });
        ArweaveViewModel arweaveViewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
        mBinding.left.setOnClickListener((v) -> {
            navigateUp();
            navigate(R.id.chooseWatchWalletFragment);
        });
        mBinding.right.setOnClickListener((v) -> {
            AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "", token -> {
                arweaveViewModel.setToken(token);
                arweaveViewModel.getRSAPublicKey();
            }, forgetPassword);
        });
        arweaveViewModel.getGeneratingAddress().observe(this, showPending -> {
            if (showPending == null) return;
            if (showPending) {
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                showLoading("");
            } else {
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                dismissLoading();
                navigateUp();
            }
        });
    }

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
