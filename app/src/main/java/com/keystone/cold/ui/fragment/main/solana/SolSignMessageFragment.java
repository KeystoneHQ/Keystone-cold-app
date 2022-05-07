package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_SIGNATURE_UR;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_FAIL_DELAY;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_REMOVE_OBSERVERS_DELAY;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_SUCCESS_DELAY;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.FragmentSolSignMessageBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class SolSignMessageFragment extends BaseFragment<FragmentSolSignMessageBinding> {

    private SolTxViewModel viewModel;
    private SigningDialog signingDialog;
    private String rawHex;

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.fragment_sol_sign_message;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(SolTxViewModel.class);
        Bundle bundle = requireArguments();
        rawHex = bundle.getString(SIGN_DATA);
        LiveData<JSONObject> liveData = viewModel.parseRawMessage(bundle);
        liveData.observe(this, o -> onMessageParsed(liveData, o));
        viewModel.parseTxException().observe(this, this::handleParseException);
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignPersonalMessage();
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
                if (signingDialog == null) {
                    signingDialog = SigningDialog.newInstance();
                    signingDialog.show(mActivity.getSupportFragmentManager(), "");
                }
                new Handler().postDelayed(() -> signingDialog.setState(SigningDialog.STATE_FAIL), SIGN_DIALOG_FAIL_DELAY);
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            String message = null;
            try {
                message = jsonObject.getString(SolTxViewModel.KEY_DATA);
                String fromAddress = jsonObject.getString(SolTxViewModel.KEY_FROM_ADDRESS);
                mBinding.address.setText(fromAddress);
                String messageUtf8 = new String(Hex.decode(message), StandardCharsets.UTF_8);
                mBinding.message.setText(messageUtf8);
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(SolSignMessageFragment.this);
            } catch (UnsupportedOperationException e) {
                mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(SolSignMessageFragment.this);
            } catch (JSONException e) {
                e.printStackTrace();
                handleParseException(e);
            }
        } else {
            mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
            mBinding.rawMessage.setText(rawHex);
            liveData.removeObservers(SolSignMessageFragment.this);
        }
    }

    private void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.invalid_data),
                    getString(R.string.incorrect_tx_data),
                    getString(R.string.confirm),
                    null);
            viewModel.parseTxException().setValue(null);
            popBackStack(R.id.assetFragment, false);
        }
    }

    private void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(KEY_SIGNATURE_UR, signatureURString);
        navigate(R.id.action_to_solBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }
}
