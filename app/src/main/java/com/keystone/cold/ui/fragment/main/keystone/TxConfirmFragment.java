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

package com.keystone.cold.ui.fragment.main.keystone;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Arith;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.config.FeatureFlags;
import com.keystone.cold.databinding.ProgressModalBinding;
import com.keystone.cold.databinding.TxConfirmFragmentBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.encryptioncore.utils.ByteFormatter;
import com.keystone.cold.ui.BindingAdapters;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.FeeAttackChecking;
import com.keystone.cold.ui.fragment.main.TransactionItem;
import com.keystone.cold.ui.fragment.main.TransactionItemAdapter;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.util.KeyStoreUtil;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.keystone.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class TxConfirmFragment extends BaseFragment<TxConfirmFragmentBinding> {

    public static final String KEY_TX_DATA = "tx_data";
    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
    private String data;
    private KeystoneTxViewModel viewModel;
    private SigningDialog signingDialog;
    private TxEntity txEntity;
    private ModalDialog addingAddressDialog;
    private int feeAttackCheckingState;
    private FeeAttackChecking feeAttackChecking;

    @Override
    protected int setView() {
        return R.layout.tx_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.txDetail.txIdInfo.setVisibility(View.GONE);
        data = bundle.getString(KEY_TX_DATA);
        viewModel = ViewModelProviders.of(this).get(KeystoneTxViewModel.class);
        mBinding.setViewModel(viewModel);
        subscribeTxEntityState();

        mBinding.sign.setOnClickListener(v -> handleSign());

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
                            token -> {
                                viewModel.setToken(token);
                                viewModel.handleSign();
                                subscribeSignState();
                            }, forgetPassword);
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
                        token -> {
                            viewModel.setToken(token);
                            viewModel.handleSign();
                            subscribeSignState();
                        }, forgetPassword);
            }
        } else {
            navigate(R.id.action_to_home);
        }
    }

    private void subscribeTxEntityState() {
        viewModel.parseTxData(data);
        viewModel.getObservableTx().observe(this, txEntity -> {
            if (txEntity != null) {
                this.txEntity = txEntity;
                mBinding.setTx(txEntity);
                if (Coins.isBTCFamily(txEntity.getCoinCode())) {
                    mBinding.txDetail.fromRow.setVisibility(View.GONE);
                    mBinding.txDetail.arrowDown.setVisibility(View.GONE);
                }
                refreshAmount();
                refreshFromList();
                refreshReceiveList();
                refreshTokenUI();
                refreshFeeDisplay();
                refreshMemoDisplay();
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
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.invalid_data),
                        getString(R.string.incorrect_tx_data),
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

    private void refreshMemoDisplay() {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.memoLabel.setText(R.string.tag);
        } else if (Coins.isPolkadotFamily(txEntity.getCoinCode()) || txEntity.getCoinCode().equals(Coins.CFX.coinCode())) {
            mBinding.txDetail.memoInfo.setVisibility(View.GONE);
        }
    }

    private void refreshFeeDisplay() {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.feeInfo.setVisibility(View.GONE);
        } else if (Coins.isPolkadotFamily(txEntity.getCoinCode())) {
            mBinding.txDetail.feeLabel.setText(R.string.dot_tip);
        }
        checkBtcFee();
    }

    private void checkBtcFee() {
        if (txEntity.getCoinCode().equals(Coins.BTC.coinCode())) {
            try {
                Number parse = NumberFormat.getInstance().parse(txEntity.getFee().split(" ")[0]);
                if (parse != null && parse.doubleValue() > 0.01) {
                    mBinding.txDetail.fee.setTextColor(Color.RED);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshTokenUI() {
        String assetCode = null;
        try {
            assetCode = txEntity.getAmount().split(" ")[1];
        } catch (Exception ignore) {
        }
        if (TextUtils.isEmpty(assetCode)) {
            assetCode = txEntity.getCoinCode();
        }
        if (assetCode.startsWith("BTC")) {
            BindingAdapters.setIcon(mBinding.txDetail.icon,
                    "BTC");
        } else {
            BindingAdapters.setIcon(mBinding.txDetail.icon,
                    txEntity.getCoinCode(),
                    assetCode);
        }
        if (!assetCode.equals(txEntity.getCoinCode())) {
            if (assetCode.startsWith("BTC")) {
                mBinding.txDetail.coinId.setText("BTC");
            } else {
                mBinding.txDetail.coinId.setText(assetCode);
            }
        } else {
            mBinding.txDetail.coinId.setText(Coins.coinNameOfCoinId(txEntity.getCoinId()));
        }
    }

    private final DecimalFormat decimalFormat = new DecimalFormat("###################.##########");

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        if (Coins.isPolkadotFamily(txEntity.getCoinCode())) {
            double amount = Double.parseDouble(txEntity.getAmount().split(" ")[0]);
            double tip = Double.parseDouble(txEntity.getFee().split(" ")[0]);
            double value = Arith.sub(amount, tip);
            mBinding.txDetail.info.setText(decimalFormat.format(value) + " " + txEntity.getCoinCode() + "\n" + to);
            return;
        } else {
            mBinding.txDetail.info.setText(to.replace(",", "\n\n"));
        }
        List<String> changeAddresses = new ArrayList<>();
        List<TransactionItem> items = new ArrayList<>();

        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                if (output.optBoolean("isChange") && !Coins.isBTCFamily(txEntity.getCoinCode())) {
                    continue;
                }
                items.add(new TransactionItem(i,
                        output.getLong("value"),
                        output.getString("address"),
                        txEntity.getDisplayName()
                ));
                if (output.optBoolean("isChange", false)) {
                    changeAddresses.add(output.getString("address"));
                }
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter;
        if (Coins.isBTCFamily(txEntity.getCoinCode())) {
            adapter = new TransactionItemAdapter(mActivity,
                    TransactionItem.ItemType.OUTPUT, changeAddresses);
        }
        else {
            adapter = new TransactionItemAdapter(mActivity,
                    TransactionItem.ItemType.TO);
            adapter.setItems(items);
        }
        adapter.setItems(items);
        mBinding.txDetail.toList.setVisibility(View.VISIBLE);
        mBinding.txDetail.toInfo.setVisibility(View.GONE);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        if (txEntity.getCoinCode().startsWith("BTC")) {
            try {
                List<TransactionItem> items = new ArrayList<>();
                JSONArray inputs = new JSONArray(from);
                for (int i = 0; i < inputs.length(); i++) {
                    items.add(new TransactionItem(i,
                            0,
                            inputs.getJSONObject(i).getString("address"),
                            txEntity.getDisplayName()
                    ));
                }
                TransactionItemAdapter adapter = new TransactionItemAdapter(mActivity,
                        TransactionItem.ItemType.INPUT);
                adapter.setItems(items);
                mBinding.txDetail.fromList.setVisibility(View.VISIBLE);
                mBinding.txDetail.fromRow.setVisibility(View.GONE);
                mBinding.txDetail.fromList.setAdapter(adapter);
            } catch (JSONException ignore) {
            }
        } else {
            mBinding.txDetail.from.setText(from);
        }
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
                    viewModel.getSignState().postValue(KeystoneTxViewModel.STATE_NONE);
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
        viewModel.getSignState().removeObservers(this);
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



