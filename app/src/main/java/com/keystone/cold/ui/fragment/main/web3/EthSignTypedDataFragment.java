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
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.GnosisHandler;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.EthSignTypedDataBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.web3.EthBroadcastTxFragment.KEY_SIGNATURE_JSON;
import static com.keystone.cold.ui.fragment.main.web3.EthTxConfirmFragment.highLight;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class EthSignTypedDataFragment extends BaseFragment<EthSignTypedDataBinding> {

    private Web3TxViewModel viewModel;
    private SigningDialog signingDialog;
    private String fromAddress;

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.eth_sign_typed_data;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        LiveData<JSONObject> liveData = viewModel.parseEIP712TypedData(requireArguments());
        liveData.observe(this, o -> onMessageParsed(liveData, o));
        viewModel.parseTxException().observe(this, this::handleParseException);
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                boolean isLegacy = jsonObject.getBoolean("isLegacy");
                if (isLegacy) {
                    JSONArray messageData = new JSONArray(jsonObject.getString("data"));
                    mBinding.primaryTypeContainer.setVisibility(View.GONE);
                    mBinding.network.setVisibility(View.GONE);
                    mBinding.domainNameContainer.setVisibility(View.GONE);
                    mBinding.verifyingContractContainer.setVisibility(View.GONE);
                    String readableMessage = adaptLegacyData(messageData);
                    fromAddress = jsonObject.getString("fromAddress");
                    mBinding.message.setText(highLight(recognizeAddressInText(readableMessage)));
                } else {
                    JSONObject messageData = new JSONObject(jsonObject.getString("data"));
                    JSONObject domain = messageData.getJSONObject("domain");
                    mBinding.primaryType.setText(messageData.getString("primaryType"));
                    mBinding.network.setText(viewModel.getNetwork(domain.optInt("chainId", 1)));
                    mBinding.name.setText(domain.optString("name"));
                    String verifyingContract = domain.optString("verifyingContract", "");
                    updateVerifyingContract(verifyingContract);
                    String message = messageData.getJSONObject("message").toString(2);
                    mBinding.message.setText(highLight(recognizeAddressInText(message)));
                    fromAddress = jsonObject.getString("fromAddress");
                }
                liveData.removeObservers(EthSignTypedDataFragment.this);
            } catch (JSONException e) {
                e.printStackTrace();
                handleParseException(e);
            }
        }
    }

    private String adaptLegacyData(JSONArray data) throws JSONException {
        String result = "";
        for (int i = 0; i < data.length(); i++) {
            JSONObject object = data.getJSONObject(i);
            if (object.has("type") && object.has("name") && object.has("value")) {
                result += object.getString("name") + ":\n";
                result += object.getString("value") + "\n";
            } else {
                throw new JSONException("invalid legacy typed data");
            }
        }
        return result;
    }

    private void updateVerifyingContract(String verifyingContract) {
        if (TextUtils.isEmpty(verifyingContract)) {
            mBinding.verifyingContractContainer.setVisibility(View.GONE);
            return;
        }
        String ens = viewModel.loadEnsAddress(verifyingContract);
        String addressSymbol = viewModel.recognizeAddress(verifyingContract);
        if (addressSymbol != null) {
            verifyingContract = verifyingContract + String.format(" (%s)", addressSymbol);
        } else if (GnosisHandler.gnosisContractAddresses.contains(verifyingContract)) {
            verifyingContract += " (GnosisSafeProxy)";
        } else {
//            verifyingContract = verifyingContract + String.format(" [%s]", "Unknown Address");
        }
        if (!TextUtils.isEmpty(ens)) {
            mBinding.verifyingContractContainer.setVisibility(View.GONE);
            mBinding.ensToInfo.setVisibility(View.VISIBLE);
            mBinding.ens.key.setText("Verifying Contract");
            mBinding.ens.value.setText(ens);
            mBinding.ens.address.setText(highLight(verifyingContract));
        } else {
            mBinding.verifyingContract.setText(highLight(verifyingContract));
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

    private String recognizeAddressInText(String text) {
        Pattern pattern = Pattern.compile("0x[a-fA-F0-9]{40}");
        Matcher matcher = pattern.matcher(text);
        List<String> hasRepalcedAddress = new ArrayList<>();
        while (matcher.find()) {
            String address = matcher.group();
            if (hasRepalcedAddress.contains(address)) continue;
            String ens = viewModel.loadEnsAddress(address);
            String symbol = viewModel.recognizeAddress(address);
            address = Eth.Deriver.toChecksumAddress(address);
            StringBuilder result = new StringBuilder();
            if (!TextUtils.isEmpty(ens)) {
                result.append(String.format("<%s>\n", ens));
            }
            result.append(address);
            if (symbol != null) {
                result.append(String.format(" (%s)", symbol));
            } else if (address.equalsIgnoreCase(fromAddress)) {
                //do nothing
            } else {
//                result.append(" [Unknown Address]");
            }
            hasRepalcedAddress.add(address);
            text = text.replace(address, result.toString());
        }
        return text;
    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignEIP712TypedData();
                    subscribeSignState();
                }, forgetPassword);
    }

    private void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (KeystoneTxViewModel.STATE_SIGNING.equals(s)) {
                if (signingDialog != null) {
                    signingDialog.dismiss();
                }
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
