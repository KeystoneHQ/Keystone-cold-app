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

package com.keystone.cold.ui.fragment.main.polkadot;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.cold.R;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.PolkadotTxConfirmBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.PolkadotErrorDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.PolkadotViewModel;
import com.keystone.cold.viewmodel.tx.PolkadotJsTxConfirmViewModel;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PolkadotTxConfirm extends BaseFragment<PolkadotTxConfirmBinding> {

    public static final String KEY_PARSED_TRANSACTION = "key_parsed_transaction";

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
    private PolkadotJsTxConfirmViewModel viewModel;
    private PolkadotViewModel polkadotViewModel;
    private SigningDialog signingDialog;

    @Override
    protected int setView() {
        return R.layout.polkadot_tx_confirm;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle bundle = requireArguments();
        String parsedTransaction = bundle.getString(KEY_PARSED_TRANSACTION);
        String data = bundle.getString(KEY_TX_DATA);
        polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
        viewModel = ViewModelProviders.of(this).get(PolkadotJsTxConfirmViewModel.class);
        try {
            JSONObject result = new JSONObject(parsedTransaction);
            String type = result.getString("transaction_type");
            JSONArray content = result.getJSONArray("content");
            mBinding.dotTx.title.setVisibility(View.GONE);
            switch (type) {
                case "Sign": {
                    int checksum = result.getInt("checksum");
                    mBinding.dotTx.txDetail.updateUI(content);
                    mBinding.dotTx.title.setVisibility(View.VISIBLE);
                    TxEntity tx = viewModel.generateAndPostSubstrateTxV2(result, data);
                    mBinding.setTx(tx);
                    mBinding.dotTx.txDetail.bindTx(tx);
                    String signContent = polkadotViewModel.getSignContent(checksum).getString("value");
                    mBinding.sign.setOnClickListener(v -> handleSign(signContent));
                    mBinding.sign.setText(R.string.sign);
                    break;
                }
                case "Stub": {
                    int checksum = result.getInt("checksum");
                    mBinding.dotTx.txDetail.updateUI(content);
                    mBinding.sign.setText(R.string.approve);
                    mBinding.sign.setOnClickListener(v -> {
                        try {
                            polkadotViewModel.handleStub(checksum);
                            navigateUp();
                        } catch (PolkadotViewModel.PolkadotException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                }
                // "Read" has already handled in scanner
                default: {
                    ModalDialog.showCommonModal(mActivity, "Warning", "Action " + type + " is not supported currently", "OK", null);
                    navigateUp();
                }
            }
        } catch (PolkadotViewModel.PolkadotException | JSONException e) {
            e.printStackTrace();
        } catch (InvalidAccountException e) {
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.account_not_match),
                    getString(R.string.account_not_match_detail),
                    getString(R.string.confirm),
                    this::navigateUp);
        }
    }

    private void handleSign(String signContent) {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSign(signContent);
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
        String txId = viewModel.getTxId();
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        navigate(R.id.action_to_broadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
