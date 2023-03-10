package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.LiveData;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.FragmentSignMessageBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BaseTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.util.CharSetUtil;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public abstract class SignMessageFragment<V extends BaseTxViewModel> extends BaseFragment<FragmentSignMessageBinding> {

    private SigningDialog signingDialog;
    protected V viewModel;


    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.fragment_sign_message;
    }

    @Override
    protected void init(View view) {
        initViewModel();
        setupView();
    }

    protected void setupView() {
        mBinding.setCoinCode(getCoinCode());
        mBinding.setCoinName(getCoinName());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            String message = null;
            try {
                message = jsonObject.getString("data");
                String fromAddress = jsonObject.getString("fromAddress");
                mBinding.address.setText(fromAddress);
                if (CharSetUtil.isHexString(message)) {
                    byte[] messageBytes = Hex.decode(message);
                    if (CharSetUtil.isUTF8Format(messageBytes)) {
                        String messageUtf8 = new String(messageBytes, StandardCharsets.UTF_8);
                        mBinding.message.setText(messageUtf8);
                    } else {
                        mBinding.llMsgUtf8.setVisibility(View.GONE);
                    }
                } else {
                    mBinding.llMsgUtf8.setVisibility(View.GONE);
                }
                mBinding.rawMessage.setText(message);
            } catch (UnsupportedOperationException e) {
                mBinding.llMsgUtf8.setVisibility(View.GONE);
                mBinding.rawMessage.setText(message);
            } catch (Exception e) {
                e.printStackTrace();
                handleParseException(e);
            } finally {
                liveData.removeObservers(this);
            }
        }
    }

    protected abstract String getCoinName();

    protected abstract String getCoinCode();

    protected abstract void initViewModel();

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignMessage();
                    subscribeSignState();
                }, forgetPassword);
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        LiveData<JSONObject> liveData = viewModel.parseMessage(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
    }


    protected void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (BaseTxViewModel.STATE_SIGNING.equals(s)) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (BaseTxViewModel.STATE_SIGN_SUCCESS.equals(s)) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess();
                    viewModel.getSignState().setValue("");
                    viewModel.getSignState().removeObservers(this);
                }, UIConstants.SIGN_DIALOG_SUCCESS_DELAY);
            } else if (BaseTxViewModel.STATE_SIGN_FAIL.equals(s)) {
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.setState(SigningDialog.STATE_FAIL);
                    }
                }, UIConstants.SIGN_DIALOG_FAIL_DELAY);
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    viewModel.getSignState().removeObservers(this);
                }, UIConstants.SIGN_DIALOG_REMOVE_OBSERVERS_DELAY);
            }
        });
    }

    protected void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.invalid_data),
                    getString(R.string.incorrect_tx_data),
                    getString(R.string.confirm),
                    null);
            popBackStack(R.id.myAssetsFragment, false);
        }
    }

    protected void onSignSuccess() {
        String signatureURString = viewModel.getSignatureUR();
        Bundle data = new Bundle();
        data.putString(BundleKeys.SIGNATURE_UR_KEY, signatureURString);
        data.putString(BundleKeys.COIN_CODE_KEY, getCoinCode());
        navigate(R.id.action_to_broadCastTxFragment, data);
    }
}
