package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

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
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.ConnectWalletActivity;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.views.AuthenticateModal;


public class ArweaveAuthFragment extends BaseFragment<ArweaveAuthFragmentBinding> {
    public final static String AR_AUTH_RESULT_KEY = "ar_auth_result_key";
    public final static String AR_SETUP_STATUS_KEY = "ar_setup_status_key";
    public final static String AR_SETUP_INITIAL = "ar_setup_initial";
    public final static String AR_SETUP_REJECTED = "ar_setup_rejected";
    public final static String AR_SETUP_SUCCESS = "ar_setup_success";

    private ModalDialog dialog;

    @Override
    protected int setView() {
        return R.layout.arweave_auth_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle fragmentResult = new Bundle();
        fragmentResult.putString(AR_SETUP_STATUS_KEY, AR_SETUP_INITIAL);
        this.getParentFragmentManager().setFragmentResult(AR_AUTH_RESULT_KEY, fragmentResult);
        mBinding.toolbar.setNavigationOnClickListener((v) -> {
            popBack(AR_SETUP_REJECTED);
        });
        ArweaveViewModel arweaveViewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
        mBinding.left.setOnClickListener((v) -> {
            popBack(AR_SETUP_REJECTED);
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
                popBack(AR_SETUP_SUCCESS);
            }
        });
    }

    private void popBack(String status) {
        //default MainActivity
        int destination = R.id.manageCoinFragment;
        if (mActivity instanceof ConnectWalletActivity){
            destination = R.id.walletListFragment;
        } else if (mActivity instanceof SetupVaultActivity) {
            destination = R.id.setupSelectWalletFragment;
        } else {
            Bundle data = getArguments();
            if (data != null) {
                boolean isFromSign = data.getBoolean(BundleKeys.AR_AUTH_FROM_SIGN_KEY, false);
                if (isFromSign) {
                    destination = R.id.arweaveConfirmTransactionFragment;
                }
            }
        }
        Bundle fragmentResult = new Bundle();
        fragmentResult.putString(AR_SETUP_STATUS_KEY, status);
        this.getParentFragmentManager().setFragmentResult(AR_AUTH_RESULT_KEY, fragmentResult);
        popBackStack(destination, false);
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
