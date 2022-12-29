package com.keystone.cold.ui.fragment.main.arweave;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;
import static com.keystone.cold.ui.fragment.main.arweave.ArweaveTxConfirmFragment.KEY_SALT_LEN;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_SIGNATURE_UR;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_FAIL_DELAY;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_REMOVE_OBSERVERS_DELAY;
import static com.keystone.cold.ui.fragment.main.solana.SolTxConfirmFragment.SIGN_DIALOG_SUCCESS_DELAY;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.FragmentSolSignMessageBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.solana.SolSignMessageFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.ArweaveTxViewModel;
import com.keystone.cold.viewmodel.tx.SignState;
import com.keystone.cold.viewmodel.tx.SolTxViewModel;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class ArweaveSignMessageFragment extends BaseFragment<FragmentSolSignMessageBinding> {
    private ArweaveTxViewModel viewModel;
    private SigningDialog signingDialog;
    private String rawHex;
    private String messageUtf8;
    private String requestId;

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
        viewModel = ViewModelProviders.of(this).get(ArweaveTxViewModel.class);
        Bundle bundle = requireArguments();

        rawHex = bundle.getString(SIGN_DATA);
        requestId = bundle.getString(REQUEST_ID);
        int saltLen = bundle.getInt(KEY_SALT_LEN);

        try {
            messageUtf8 = new String(Hex.decode(rawHex), StandardCharsets.UTF_8);
            mBinding.message.setText(messageUtf8);
            mBinding.rawMessage.setText(rawHex);
        } catch (UnsupportedOperationException e) {
            mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
            mBinding.rawMessage.setText(rawHex);
        }

        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> handleSign(rawHex, saltLen));
        mBinding.address.setVisibility(View.GONE);
        mBinding.rawMessage.setText(rawHex);
        mBinding.message.setText(messageUtf8);
    }

    private void handleSign(String message, int saltLen) {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    subscribeSignState(viewModel.handleSignMessage(message, saltLen));
                }, forgetPassword);
    }

    private void subscribeSignState(MutableLiveData<SignState> signState) {
        signState.observe(this, s -> {
            if (SignState.STATE_SIGNING.equals(s.getStatus())) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (SignState.STATE_SIGN_SUCCESS.equals(s.getStatus())) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess(s.getSignature());
                }, SIGN_DIALOG_SUCCESS_DELAY);
            } else if (SignState.STATE_SIGN_FAIL.equals(s.getStatus())) {
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
                    signState.removeObservers(this);
                }, SIGN_DIALOG_REMOVE_OBSERVERS_DELAY);
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void onSignSuccess(String signature) {
        Bundle data = new Bundle();
        UUID uuid = UUID.fromString(requestId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] requestId = byteBuffer.array();
        ArweaveSignature arweaveSignature = new ArweaveSignature(Hex.decode(signature), requestId);
        data.putString(KEY_SIGNATURE_UR, arweaveSignature.toUR().toString());
        navigate(R.id.action_to_arweaveBroadcastFragment, data);
    }
}
