package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.GnosisHandler;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AbiItemBinding;
import com.keystone.cold.databinding.AbiItemMethodBinding;
import com.keystone.cold.databinding.EnsItemBinding;
import com.keystone.cold.databinding.FragmentEthereumTxBinding;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.tx.GenericETHTxEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthereumTransactionDetailFragment extends BaseFragment<FragmentEthereumTxBinding> {
    private EthereumTxViewModel viewModel;
    public static Pattern patternEns = Pattern.compile("(?<=\\<)[^\\>]+");
    public static Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
    public static Pattern pattern1 = Pattern.compile("(?<=\\[)[^]]+");
    public static int MAX_PER_GAS = 1000;

    private GenericETHTxEntity genericETHTxEntity;

    public static EthereumTransactionDetailFragment newInstance(@NonNull Bundle bundle) {
        EthereumTransactionDetailFragment fragment = new EthereumTransactionDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_ethereum_tx;
    }

    @Override
    protected void init(View view) {

        mBinding.checkInfo.setVisibility(View.VISIBLE);
        mBinding.info.setOnClickListener(view1 -> realShowDialog());
        viewModel.parseTxData(requireArguments());

        viewModel = ViewModelProviders.of(getParentFragment()).get(EthereumTxViewModel.class);
        viewModel.getObservableEthTx().observe(this, ethTxEntity -> {
            this.genericETHTxEntity = ethTxEntity;
            if (this.genericETHTxEntity != null) {
                updateUI();
            }
        });
        viewModel.getObservableEthTx().observe(this, genericETHTxEntity -> {
            if (genericETHTxEntity != null) {
                if (viewModel.getGasPrice(genericETHTxEntity.getFeeValue(), genericETHTxEntity.getGasLimitValue()).doubleValue() > MAX_PER_GAS) {
                    viewModel.setExceeded(true);
                }
                updateUI();
            }
        });
        viewModel.parseTxException().observe(this, this::handleParseException);
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

    private void handleParseException(BaseException ex) {
        if (ex != null) {
            ex.printStackTrace();
            alertException(ex, () -> {
                popBackStack(R.id.assetFragment, false);
            });
            viewModel.parseTxException().setValue(null);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI() {
        updateNetworkName();
        mBinding.icon.setImageDrawable(mActivity.getDrawable(genericETHTxEntity.getIcon()));
        if (viewModel.isExceeded()) {
            mBinding.fee.setTextColor(Color.RED);
            mBinding.feeTooHigh.setVisibility(View.VISIBLE);
        }
        JSONObject abi = viewModel.getAbi();
        if (abi != null) {
            updateAbiView(abi);
            mBinding.data.setVisibility(View.VISIBLE);
            mBinding.undecodedData.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(viewModel.getInputData())) {
                mBinding.data.setVisibility(View.GONE);
                mBinding.undecodedData.setVisibility(View.VISIBLE);
                mBinding.inputData.setText("0x" + viewModel.getInputData());
                if (!TextUtils.isEmpty(viewModel.getSelectorMethodName())) {
                    updateSelectorView();
                }
                showDialog();
            } else {
                mBinding.data.setVisibility(View.GONE);
                mBinding.undecodedData.setVisibility(View.GONE);
            }
        }
        mBinding.setTx(genericETHTxEntity);
        processAndUpdateTo();
    }

    private void updateSelectorView() {
        mBinding.tvSelector.setText(viewModel.getSelectorMethodName());
        mBinding.llSelector.setVisibility(View.VISIBLE);
    }

    private void updateNetworkName() {
        mBinding.network.setText(viewModel.getNetwork(viewModel.getChainId()));
    }

    private void processAndUpdateTo() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String to = genericETHTxEntity.getTo();
            String ens = viewModel.loadEnsAddress(to);
            String addressSymbol = viewModel.recognizeAddress(to);
            if (!TextUtils.isEmpty(addressSymbol)) {
                to = to + String.format(" (%s)", addressSymbol);
            } else if (GnosisHandler.gnosisContractAddresses.contains(to.toLowerCase())) {
                to += " (Safe)";
            } else {
//                to = to + String.format(" [%s]", "Unknown Address");
            }
            String finalTo = to;
            AppExecutors.getInstance().mainThread().execute(() -> {
                mBinding.to.setText(highLight(finalTo));
                if (!TextUtils.isEmpty(ens)) {
                    mBinding.toInfo.setVisibility(View.GONE);
                    mBinding.ensToInfo.setVisibility(View.VISIBLE);
                    mBinding.ens.key.setText(getString(R.string.tx_to));
                    mBinding.ens.value.setText(ens);
                    mBinding.ens.address.setText(highLight(finalTo));
                } else {
                    mBinding.to.setText(highLight(finalTo));
                }
            });
        });
    }

    private void updateAbiView(JSONObject abi) {
        if (abi != null) {
            if (viewModel.isFromTFCard()) {
                mBinding.tfcardTip.setVisibility(View.VISIBLE);
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
                        mBinding.container.addView(abiItemMethodBinding.getRoot());
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
                mBinding.container.addView(abiItemMethodBinding.getRoot());
                continue;
            }
            if ("address".equals(item.type)) {
                String ens = viewModel.loadEnsAddress(item.value);
                String addressSymbol = viewModel.recognizeAddress(item.value);
                if (addressSymbol == null) {
                    if (GnosisHandler.gnosisContractAddresses.contains(item.value.toLowerCase())) {
                        addressSymbol = "Safe";
                    }
                }
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
                    mBinding.container.addView(ensBinding.getRoot());
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
            mBinding.container.addView(binding.getRoot());
        }
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
