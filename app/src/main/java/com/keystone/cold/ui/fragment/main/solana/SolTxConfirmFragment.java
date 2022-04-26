package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.main.web3.EthBroadcastTxFragment.KEY_SIGNATURE_JSON;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.SolTxConfirmBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.callback.ParseCallback;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

import org.json.JSONException;
import org.json.JSONObject;

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
        Bundle bundle = requireArguments();
        mBinding.rawMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
        viewModel.parseTxData(bundle, new ParseCallback(){
            @Override
            public void OnSuccess(String json) {
                try{
                    JSONObject tx = new JSONObject(json);
                    mBinding.rawMessage.setText(tx.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed() {

            }
        });


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



    SigningDialog signingDialog;
    private void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (KeystoneTxViewModel.STATE_SIGNING.equals(s)) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (KeystoneTxViewModel.STATE_SIGN_SUCCESS.equals(s)) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess();
                }, 500);
            } else if (KeystoneTxViewModel.STATE_SIGN_FAIL.equals(s)) {
                if (signingDialog == null) {
                    signingDialog = SigningDialog.newInstance();
                    signingDialog.show(mActivity.getSupportFragmentManager(), "");
                }
                new Handler().postDelayed(() -> signingDialog.setState(SigningDialog.STATE_FAIL), 1000);
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    viewModel.getSignState().removeObservers(this);
                }, 2000);
            }
        });
    }

    private void onSignSuccess() {
        String txId = ""; //viewModel.getTxId();
        String signature = viewModel.getSignatureJson();
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        data.putString(KEY_SIGNATURE_JSON, signature);
        navigate(R.id.action_to_ethBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
