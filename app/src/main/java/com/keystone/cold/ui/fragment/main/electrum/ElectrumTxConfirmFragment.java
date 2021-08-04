/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.ui.fragment.main.electrum;

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.keystone.coinlib.utils.Base43;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.databinding.ElectrumTxConfirmFragmentBinding;
import com.keystone.cold.databinding.ExportSdcardModalBinding;
import com.keystone.cold.databinding.ProgressModalBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.FeeAttackChecking;
import com.keystone.cold.ui.fragment.main.TransactionItem;
import com.keystone.cold.ui.fragment.main.TransactionItemAdapter;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.update.utils.Storage;
import com.keystone.cold.util.KeyStoreUtil;
import com.keystone.cold.viewmodel.ElectrumViewModel;
import com.keystone.cold.viewmodel.tx.ElectrumTxViewModel;
import com.keystone.cold.viewmodel.exceptions.XpubNotMatchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.keystone.cold.update.utils.Storage.hasSdcard;
import static com.keystone.cold.viewmodel.ElectrumViewModel.exportSuccess;
import static com.keystone.cold.viewmodel.ElectrumViewModel.showNoSdcardModal;
import static com.keystone.cold.viewmodel.ElectrumViewModel.writeToSdcard;
import static com.keystone.cold.viewmodel.tx.KeystoneTxViewModel.STATE_NONE;

public class ElectrumTxConfirmFragment extends BaseFragment<ElectrumTxConfirmFragmentBinding> {

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
    private ElectrumTxViewModel viewModel;
    private SigningDialog signingDialog;
    private TxEntity txEntity;
    private ModalDialog addingAddressDialog;
    private String txnData;
    private List<String> changeAddress = new ArrayList<>();
    private int feeAttackCheckingState;
    private FeeAttackChecking feeAttackChecking;

    public static void showExportTxnDialog(AppCompatActivity activity, String txId, String hex,
                                           Runnable onExportSuccess) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.export_sdcard_modal, null, false);
        String fileName = "signed_" + txId.substring(0, 8) + ".txn";
        binding.title.setText(R.string.export_signed_txn);
        binding.fileName.setText(fileName);
        binding.actionHint.setText(R.string.electrum_import_signed_txn);
        binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
        binding.confirm.setOnClickListener(vv -> {
            modalDialog.dismiss();
            if (hasSdcard()) {
                Storage storage = Storage.createByEnvironment();
                boolean result = writeToSdcard(storage, generateElectrumTxn(hex), fileName);
                if (result) {
                    exportSuccess(activity, onExportSuccess);
                }
            } else {
                showNoSdcardModal(activity);
            }
        });
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    private static String generateElectrumTxn(String hex) {
        JSONObject txn = new JSONObject();
        try {
            txn.put("hex", hex);
            txn.put("complete", true);
            txn.put("final", ElectrumTx.isFinal(hex));
            return txn.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected int setView() {
        return R.layout.electrum_tx_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.txDetail.txIdInfo.setVisibility(View.GONE);
        mBinding.txDetail.export.setVisibility(View.GONE);
        mBinding.txDetail.qr.setVisibility(View.GONE);
        txnData = bundle.getString("txn");
        viewModel = ViewModelProviders.of(this).get(ElectrumTxViewModel.class);
        mBinding.setViewModel(viewModel);
        subscribeTxEntityState();
        mBinding.sign.setOnClickListener(v -> handleSign());
        ViewModelProviders.of(mActivity)
                .get(ElectrumViewModel.class)
                .getChangeAddress()
                .observe(this, address -> this.changeAddress = address);
    }

    private void handleSign() {
        if (feeAttackCheckingState == SAME_OUTPUTS) {
            feeAttackChecking.showFeeAttackWarning();
            return;
        }
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        if (txEntity != null) {
            if (FeatureFlags.ENABLE_WHITE_LIST) {
                if (isAddressInWhiteList()) {
                    AuthenticateModal.show(mActivity,
                            getString(R.string.password_modal_title),
                            "",
                            fingerprintSignEnable,
                            signWithVerifyInfo(), forgetPassword);
                } else {
                    Utilities.alert(mActivity, getString(R.string.hint),
                            getString(R.string.not_in_whitelist_reject),
                            getString(R.string.confirm),
                            () -> navigate(R.id.action_to_home));
                }

            } else {
                AuthenticateModal.show(mActivity,
                        getString(R.string.password_modal_title),
                        "",
                        fingerprintSignEnable,
                        signWithVerifyInfo(), forgetPassword);
            }
        } else {
            navigate(R.id.action_to_home);
        }
    }

    private AuthenticateModal.OnVerify signWithVerifyInfo() {
        return token -> {
            viewModel.setToken(token);
            viewModel.handleSign();
            subscribeSignState();
        };
    }

    private void subscribeTxEntityState() {
        ProgressModalDialog dialog = new ProgressModalDialog();
        dialog.show(mActivity.getSupportFragmentManager(), "");
        viewModel.parseTxnData(txnData);
        viewModel.getObservableTx().observe(this, txEntity -> {
            if (txEntity != null) {
                dialog.dismiss();
                this.txEntity = txEntity;
                mBinding.setTx(txEntity);
                refreshAmount();
                refreshFromList();
                refreshReceiveList();
            }
        });

        viewModel.getAddingAddressState().observe(this, b -> {
            if (b) {
                addingAddressDialog = ModalDialog.newInstance();
                ProgressModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.progress_modal, null, false);
                binding.text.setText(R.string.sync_in_progress);
                binding.text.setVisibility(View.VISIBLE);
                addingAddressDialog.setBinding(binding);
                addingAddressDialog.show(mActivity.getSupportFragmentManager(), "");
            } else {
                if (addingAddressDialog != null) {
                    addingAddressDialog.dismiss();
                }
            }
        });

        viewModel.parseTxException().observe(this, ex -> {
            if (ex != null) {
                ex.printStackTrace();
                dialog.dismiss();

                String errorMessage = getString(R.string.incorrect_tx_data);
                if (ex instanceof XpubNotMatchException) {
                    errorMessage = getString(R.string.master_pubkey_not_match);
                }
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.electrum_decode_txn_fail),
                        errorMessage,
                        getString(R.string.confirm),
                        null);
                navigateUp();
            }
        });

        viewModel.feeAttackChecking().observe(this, state -> {
            feeAttackCheckingState = state;
            if (state != NORMAL) {
                feeAttackChecking = new FeeAttackChecking(this);
            }
        });
    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                items.add(new TransactionItem(i,
                        outputs.getJSONObject(i).getLong("value"),
                        outputs.getJSONObject(i).getString("address"),
                        Coins.BTC.coinCode()
                ));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter
                = new TransactionItemAdapter(mActivity,
                TransactionItem.ItemType.OUTPUT,
                changeAddress);
        adapter.setItems(items);
        mBinding.txDetail.toList.setVisibility(View.VISIBLE);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray inputs = new JSONArray(from);
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject out = inputs.getJSONObject(i);
                items.add(new TransactionItem(i,
                        out.getLong("value"),
                        out.getString("address"),
                        Coins.BTC.coinCode()));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter
                = new TransactionItemAdapter(mActivity,
                TransactionItem.ItemType.INPUT);
        adapter.setItems(items);
        mBinding.txDetail.fromList.setVisibility(View.VISIBLE);
        mBinding.txDetail.fromList.setAdapter(adapter);
    }

    private void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (ElectrumTxViewModel.STATE_SIGNING.equals(s)) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (ElectrumTxViewModel.STATE_SIGN_SUCCESS.equals(s)) {
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
            } else if (ElectrumTxViewModel.STATE_SIGN_FAIL.equals(s)) {
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
                    viewModel.getSignState().setValue(STATE_NONE);
                    viewModel.getSignState().removeObservers(this);
                }, 2000);
            }
        });
    }

    private void onSignSuccess() {
        handleTxnSignSuccess();
        viewModel.getSignState().removeObservers(this);
    }

    private void handleTxnSignSuccess() {
        String hex = viewModel.getTxHex();
        String base43 = Base43.encode(Hex.decode(hex));
        if (base43.length() <= 1000) {
            String txId = viewModel.getTxId();
            Bundle data = new Bundle();
            data.putString(KEY_TXID, txId);
            navigate(R.id.action_to_broadcastElectrumTxFragment, data);
        } else {
            showExportTxnDialog(mActivity, viewModel.getTxId(),
                    viewModel.getTxHex(), this::navigateUp);
        }
    }

    private boolean isAddressInWhiteList() {
        String to = txEntity.getTo();
        String encryptedAddress = ByteFormatter.bytes2hex(
                new KeyStoreUtil().encrypt(to.getBytes(StandardCharsets.UTF_8)));
        return viewModel.isAddressInWhiteList(encryptedAddress);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}



