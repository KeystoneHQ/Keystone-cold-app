package com.keystone.cold.ui.fragment.main.solana;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_SIGNATURE_UR;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.SolTxConfirmBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

public class SolTxConfirmFragment extends BaseFragment<SolTxConfirmBinding> {

    public static final int SIGN_DIALOG_SUCCESS_DELAY = 500;
    public static final int SIGN_DIALOG_FAIL_DELAY = 1000;
    public static final int SIGN_DIALOG_REMOVE_OBSERVERS_DELAY = 2000;

    private SigningDialog signingDialog;
    private SolTxViewModel viewModel;

    private Fragment[] fragments;
    private Bundle bundle;

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
        bundle = requireArguments();
        viewModel = ViewModelProviders.of(this).get(SolTxViewModel.class);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> {
            handleSign();
        });
        viewModel.parseTxData(bundle);
        initViewPager();
    }

    private void initViewPager() {
        String[] title = {getString(R.string.raw)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = SolRawTxFragment.newInstance(bundle);
        }

        mBinding.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewPager);
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
                    subscribeSignState();
                }, forgetPassword);
    }


    private void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (SolTxViewModel.STATE_SIGNING.equals(s)) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (SolTxViewModel.STATE_SIGN_SUCCESS.equals(s)) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess();
                }, SIGN_DIALOG_SUCCESS_DELAY);
            } else if (SolTxViewModel.STATE_SIGN_FAIL.equals(s)) {
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.setState(SigningDialog.STATE_FAIL);
                    }
                }, SIGN_DIALOG_FAIL_DELAY);
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    viewModel.getSignState().removeObservers(this);
                }, SIGN_DIALOG_REMOVE_OBSERVERS_DELAY);
            }
        });
    }

    private void onSignSuccess() {
        String txId = viewModel.getTxId();
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        data.putString(KEY_SIGNATURE_UR, signatureURString);
        navigate(R.id.action_to_solBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
