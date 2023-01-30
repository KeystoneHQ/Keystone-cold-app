package com.keystone.cold.ui.fragment.main.arweave;

import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.keystone.cold.R;
import com.keystone.cold.databinding.ArweaveAuthFragmentBinding;
import com.keystone.cold.databinding.ProgressModalBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.views.AuthenticateModal;


public class ArweaveAuthFragment extends BaseFragment<ArweaveAuthFragmentBinding> {

    private ModalDialog dialog;

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
                showProgress();
            } else {
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                dismissProgress();
                navigateUp();
            }
        });
    }

    private void showProgress() {
        ProgressModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.progress_modal, null, false);
        dialog = ModalDialog.newInstance();
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
        String[] steps = mActivity.getResources().getStringArray(R.array.generate_arweave_address_step);
        binding.text.setText(steps[0]);
        binding.text.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 20000);
                    if (i < steps.length) binding.text.setText(steps[i]);
                    i++;
                    if (i > steps.length - 1) {
                        handler.removeCallbacks(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.post(runnable);
    }

    private void dismissProgress() {
        if (dialog != null) {
            dialog.dismiss();
        }
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
