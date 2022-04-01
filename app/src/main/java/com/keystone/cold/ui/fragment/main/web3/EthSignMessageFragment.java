/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.ui.fragment.main.web3;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.EthSignMessageBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.web3.EthBroadcastTxFragment.KEY_SIGNATURE_JSON;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class EthSignMessageFragment extends BaseFragment<EthSignMessageBinding> {

    private Web3TxViewModel viewModel;
    private SigningDialog signingDialog;

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.eth_sign_message;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        LiveData<JSONObject> liveData = viewModel.parseRawMessage(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
        viewModel.parseTxException().observe(this, this::handleParseException);
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            String message = null;
            try {
                message = jsonObject.getString("data");
                String fromAddress = jsonObject.getString("fromAddress");
                mBinding.address.setText(fromAddress);
                String messageUtf8 = new String(Hex.decode(message), StandardCharsets.UTF_8);
                mBinding.message.setText(messageUtf8);
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(EthSignMessageFragment.this);
            } catch (UnsupportedOperationException e) {
                mBinding.message.setText(R.string.decode_as_utf8_failed_hint);
                mBinding.rawMessage.setText(message);
                liveData.removeObservers(EthSignMessageFragment.this);
            } catch (JSONException e) {
                e.printStackTrace();
                handleParseException(e);
            }
        }
    }

    private boolean isGarbled(String messageUtf8) {
        try {
            Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
            Matcher m = p.matcher(messageUtf8);
            String after = m.replaceAll("");
            String temp = after.replaceAll("\\p{P}", "");
            char[] ch = temp.trim().toCharArray();

            int length = (ch != null) ? ch.length : 0;
            for (int i = 0; i < length; i++) {
                char c = ch[i];
                if (!Character.isLetterOrDigit(c)) {
                    String str = "" + ch[i];
                    if (!str.matches("[\u4e00-\u9fa5]+")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
        String signature = viewModel.getSignatureJson();
        Bundle data = new Bundle();
        data.putString(KEY_SIGNATURE_JSON, signature);
        navigate(R.id.action_to_ethBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
