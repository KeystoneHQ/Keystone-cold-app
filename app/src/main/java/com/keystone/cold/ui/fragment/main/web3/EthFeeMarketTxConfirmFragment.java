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

import static com.keystone.cold.callables.FingerprintPolicyCallable.READ;
import static com.keystone.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment.KEY_TXID;
import static com.keystone.cold.ui.fragment.main.web3.EthBroadcastTxFragment.KEY_SIGNATURE_JSON;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.GnosisHandler;
import com.keystone.coinlib.exception.InvalidETHAccountException;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.FingerprintPolicyCallable;
import com.keystone.cold.databinding.AbiItemBinding;
import com.keystone.cold.databinding.AbiItemMethodBinding;
import com.keystone.cold.databinding.EnsItemBinding;
import com.keystone.cold.databinding.EthFeeMarketTxConfirmBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.SigningDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;
import com.keystone.cold.viewmodel.tx.KeystoneTxViewModel;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthFeeMarketTxConfirmFragment extends BaseFragment<EthFeeMarketTxConfirmBinding> {
    private Web3TxViewModel viewModel;
    private SigningDialog signingDialog;
    private GenericETHTxEntity genericETHTxEntity;
    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    public static Pattern patternEns = Pattern.compile("(?<=\\<)[^\\>]+");
    public static Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
    public static Pattern pattern1 = Pattern.compile("(?<=\\[)[^]]+");
    public static int MAX_PRIORITY_PER_GAS = 1000;
    public static int MAX_FEE_PER_GAS = 10000;
    private boolean isExceeded;


    @Override
    protected int setView() {
        return R.layout.eth_fee_market_tx_confirm;
    }

    @Override
    protected void init(View view) {
        mBinding.ethTx.checkInfo.setVisibility(View.VISIBLE);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        mBinding.sign.setOnClickListener(v -> checkExceedFeeDialog());
        mBinding.ethTx.info.setOnClickListener(view1 -> realShowDialog());
        viewModel.parseEIP1559TxData(requireArguments());
    }

    private void showDialog() {
        int visits = Utilities.getVisitsTimes(mActivity);
        if (visits++ == 0) {
            realShowDialog(getString(R.string.learn_more));
            Utilities.setVisitsTimes(mActivity, visits);
        }
    }

    private void realShowDialog() {
        ModalDialog.showCommonModal((AppCompatActivity) getActivity(),
                getString(R.string.tip),
                getString(R.string.learn_more_doc),
                getString(R.string.know),
                null);
    }

    private void realShowDialog(String subtitle) {
        ModalDialog.showCommonModal((AppCompatActivity) getActivity(),
                getString(R.string.tip),
                subtitle,
                getString(R.string.know),
                null);
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
                    viewModel.handleSignFeeMarket();
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
        String signature = viewModel.getSignatureJson();
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        data.putString(KEY_SIGNATURE_JSON, signature);
        navigate(R.id.action_to_ethBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI() {
        mBinding.ethTx.network.setText(viewModel.getNetwork(viewModel.getChainId()));
        String feeEstimatedContent = String.format("Max Priority fee (%s) * Gas limit (%s)",
                genericETHTxEntity.getMaxPriorityFeePerGas(), genericETHTxEntity.getGasLimit());
        String feeMaxContent = String.format("Max fee (%s) * Gas limit (%s)",
                genericETHTxEntity.getMaxFeePerGas(), genericETHTxEntity.getGasLimit());
        mBinding.ethTx.icon.setImageDrawable(mActivity.getDrawable(genericETHTxEntity.getChainId() == 1 ?
                R.drawable.coin_eth : R.drawable.coin_eth_token));
        if (isExceeded) {
            mBinding.ethTx.maxFeeTooHigh.setVisibility(View.VISIBLE);
            mBinding.ethTx.priorityFeeTooHigh.setVisibility(View.VISIBLE);
            mBinding.ethTx.feeEstimatedValue.setTextColor(Color.RED);
            mBinding.ethTx.feeMaxValue.setTextColor(Color.RED);
            SpannableStringBuilder spannableMaxEstimated = new SpannableStringBuilder(feeEstimatedContent);
            spannableMaxEstimated.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.red)), 18,
                    18 + genericETHTxEntity.getMaxPriorityFeePerGas().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            SpannableStringBuilder spannableMaxFee = new SpannableStringBuilder(feeMaxContent);
            spannableMaxFee.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.red)), 9,
                    9 + genericETHTxEntity.getMaxPriorityFeePerGas().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mBinding.ethTx.feeEstimatedDetail.setText(spannableMaxEstimated);
            mBinding.ethTx.feeMaxDetail.setText(spannableMaxFee);
        } else {
            mBinding.ethTx.feeEstimatedDetail.setText(feeEstimatedContent);
            mBinding.ethTx.feeMaxDetail.setText(feeMaxContent);
        }
        JSONObject abi = viewModel.getAbi();
        if (abi != null) {
            updateAbiView(abi);
            mBinding.ethTx.data.setVisibility(View.VISIBLE);
            mBinding.ethTx.undecodedData.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(viewModel.getInputData())) {
                mBinding.ethTx.data.setVisibility(View.GONE);
                mBinding.ethTx.undecodedData.setVisibility(View.VISIBLE);
                mBinding.ethTx.inputData.setText("0x" + viewModel.getInputData());
                if (!TextUtils.isEmpty(viewModel.getSelectorMethodName())) {
                    updateSelectorView();
                }
                showDialog();
            } else {
                mBinding.ethTx.data.setVisibility(View.GONE);
                mBinding.ethTx.undecodedData.setVisibility(View.GONE);
            }
        }
        mBinding.setTx(genericETHTxEntity);
        processAndUpdateTo();
    }

    private void updateSelectorView() {
        mBinding.ethTx.tvSelector.setText(viewModel.getSelectorMethodName());
        mBinding.ethTx.llSelector.setVisibility(View.VISIBLE);
    }

    private void processAndUpdateTo() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String to = genericETHTxEntity.getTo();
            String ens = viewModel.loadEnsAddress(to);
            String addressSymbol = viewModel.recognizeAddress(to);
            if (!TextUtils.isEmpty(addressSymbol)) {
                to = to + String.format(" (%s)", addressSymbol);
            } else if (GnosisHandler.gnosisContractAddresses.contains(to.toLowerCase())) {
                to += " (GnosisSafeProxy)";
            } else {
//                to = to + String.format(" [%s]", "Unknown Address");
            }
            String finalTo = to;
            AppExecutors.getInstance().mainThread().execute(() -> {
                mBinding.ethTx.to.setText(highLight(finalTo));
                if (!TextUtils.isEmpty(ens)) {
                    mBinding.ethTx.toInfo.setVisibility(View.GONE);
                    mBinding.ethTx.ensToInfo.setVisibility(View.VISIBLE);
                    mBinding.ethTx.ens.key.setText(getString(R.string.tx_to));
                    mBinding.ethTx.ens.value.setText(ens);
                    mBinding.ethTx.ens.address.setText(highLight(finalTo));
                } else {
                    mBinding.ethTx.to.setText(highLight(finalTo));
                }
            });
        });
    }

    private void updateAbiView(JSONObject abi) {
        if (abi != null) {
            if (viewModel.isFromTFCard()) {
                mBinding.ethTx.tfcardTip.setVisibility(View.VISIBLE);
            }
            String contract = abi.optString("contract");
            boolean isUniswap = contract.toLowerCase().contains("uniswap");
            AppExecutors.getInstance().diskIO().execute(() -> {
                List<AbiItemAdapter.AbiItem> itemList = new AbiItemAdapter(genericETHTxEntity.getFrom(), viewModel).adapt(abi);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (itemList == null) {
                        AbiItemMethodBinding abiItemMethodBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                                R.layout.abi_item_method, null, false);
                        try {
                            abiItemMethodBinding.value.setText(abi.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mBinding.ethTx.container.addView(abiItemMethodBinding.getRoot());
                    } else {
                        addViewToData(isUniswap, itemList);
                    }
                });
            });
        }
    }

    private void addViewToData(boolean isUniswap, List<AbiItemAdapter.AbiItem> itemList) {
        for (int i = 0; i < itemList.size(); i++) {
            AbiItemAdapter.AbiItem item = itemList.get(i);
            if ("method".equals(item.key)) {
                AbiItemMethodBinding abiItemMethodBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.abi_item_method, null, false);
                abiItemMethodBinding.key.setText(item.key);
                abiItemMethodBinding.value.setText(item.value);
                if (i == 0) {
                    abiItemMethodBinding.divider.setVisibility(View.GONE);
                }
                mBinding.ethTx.container.addView(abiItemMethodBinding.getRoot());
                continue;
            }
            if ("address".equals(item.type)) {
                String ens = viewModel.loadEnsAddress(item.value);
                String addressSymbol = viewModel.recognizeAddress(item.value);
                item.value = Eth.Deriver.toChecksumAddress(item.value);
                if (addressSymbol != null) {
                    item.value += String.format(" (%s)", addressSymbol);
                } else if (!"to".equals(item.key)) {
//                            item += String.format(" [%s]", "Unknown Address");
                }
                if (!TextUtils.isEmpty(ens)) {
                    EnsItemBinding ensBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                            R.layout.ens_item, null, false);
                    ensBinding.key.setText(item.key);
                    ensBinding.value.setText(ens);
                    ensBinding.address.setText(highLight(item.value));
                    mBinding.ethTx.container.addView(ensBinding.getRoot());
                    continue;
                }
            }

            AbiItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.abi_item, null, false);
            binding.key.setText(item.key);
            if (isUniswap && "to".equals(item.key)) {
                if (!item.value.equalsIgnoreCase(genericETHTxEntity.getFrom())) {
                    item.value += String.format(" [%s]", getString(R.string.inconsistent_address));
                }
            }
            binding.value.setText(highLight(item.value));
            mBinding.ethTx.container.addView(binding.getRoot());
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        viewModel.getObservableEthTx().observe(this, genericETHTxEntity -> {
            this.genericETHTxEntity = genericETHTxEntity;
            if (this.genericETHTxEntity != null) {
                double maxPriorityFee = genericETHTxEntity.getMaxPriorityFeePerGasValue().doubleValue();
                boolean isMaxPriorityFeeExceeded = maxPriorityFee > MAX_PRIORITY_PER_GAS;
                double maxfee = genericETHTxEntity.getMaxFeePerGasValue().doubleValue();
                boolean isMaxFeeExceeded = maxfee > MAX_FEE_PER_GAS;
                isExceeded = isMaxPriorityFeeExceeded || isMaxFeeExceeded;
                updateUI();
            }
        });
        viewModel.parseTxException().observe(this, this::handleParseException);
    }

    private void checkExceedFeeDialog() {
        if (isExceeded) {
            ModalDialog.showTwoButtonCommonModal(mActivity,
                    getString(R.string.atention),
                    getString(R.string.exceed_fee),
                    getString(R.string.sign),
                    getString(R.string.cancel),
                    this::handleSign,
                    null);
            return;
        }
        handleSign();
    }

    public static SpannableStringBuilder highLight(String content) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(content);

        Matcher matcherEns = patternEns.matcher(spannable);
        while (matcherEns.find()) {
            spannable.replace(matcherEns.start() - 1, matcherEns.start(), " ");
            spannable.replace(matcherEns.end(), matcherEns.end() + 1, " ");
            spannable.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.ens)), matcherEns.start(),
                    matcherEns.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        Matcher matcher = pattern.matcher(spannable);
        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.icon_select)), matcher.start() - 1,
                    matcher.end() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (content.contains("Unknown") || content.contains("Inconsistent")) {
            matcher = pattern1.matcher(spannable);
            while (matcher.find()) {
                spannable.replace(matcher.start() - 1, matcher.start(), "(");
                spannable.replace(matcher.end(), matcher.end() + 1, ")");
                spannable.setSpan(new ForegroundColorSpan(Color.RED), matcher.start() - 1,
                        matcher.end() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return spannable;
    }
}
