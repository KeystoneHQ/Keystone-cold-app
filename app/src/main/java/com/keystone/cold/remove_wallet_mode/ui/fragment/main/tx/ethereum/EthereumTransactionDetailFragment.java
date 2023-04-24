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

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

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
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthereumTransactionDetailFragment extends BaseFragment<FragmentEthereumTxBinding> {
    public static Pattern patternEns = Pattern.compile("(?<=\\<)[^\\>]+");
    public static Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
    public static Pattern pattern1 = Pattern.compile("(?<=\\[)[^]]+");

    private final MutableLiveData<EthereumTransaction> transaction;

    public EthereumTransactionDetailFragment(MutableLiveData<EthereumTransaction> transaction) {
        this.transaction = transaction;
    }

    public static EthereumTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<EthereumTransaction> transaction) {
        EthereumTransactionDetailFragment fragment = new EthereumTransactionDetailFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_ethereum_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.info.setOnClickListener(view1 -> realShowDialog());
        transaction.observe(this, this::updateUI);
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(EthereumTransaction transaction) {
        if (transaction == null) return;
        AssetItem assetItem = transaction.getAssetItem();
        if (assetItem != null) {
            mBinding.setCheckInfoTitle(assetItem.getNetwork());
            mBinding.setCoinCode(assetItem.getCoinCode());
        } else {
            mBinding.setCheckInfoTitle(EthereumTxViewModel.getNetwork(transaction.getChainId()));
            String coinCode = EthereumTxViewModel.getIconCode(transaction.getChainId());
            if (coinCode != null) {
                mBinding.setCoinCode(coinCode);
            } else {
                mBinding.checkInfoLayout.icon.setImageResource(R.drawable.coin_eth_token);
            }
        }

        if (transaction.getTxType() == EthereumTransaction.TransactionType.LEGACY.getType()) {
            mBinding.legacyFeeInfo.setVisibility(View.VISIBLE);
            if (transaction.isFeeExceeded()) {
                mBinding.fee.setTextColor(Color.RED);
                mBinding.feeTooHigh.setVisibility(View.VISIBLE);
            }
        }
        if (transaction.getTxType() == EthereumTransaction.TransactionType.FEE_MARKET.getType()) {
            mBinding.eip1559FeeEstimated.setVisibility(View.VISIBLE);
            mBinding.eip1559FeeMax.setVisibility(View.VISIBLE);
            String feeEstimatedContent = String.format("Max Priority fee (%s) * Gas limit (%s)",
                    transaction.getMaxPriorityFeePerGas(), transaction.getGasLimit());
            String feeMaxContent = String.format("Max fee (%s) * Gas limit (%s)",
                    transaction.getMaxFeePerGas(), transaction.getGasLimit());
            if (transaction.isFeeExceeded()) {
                mBinding.maxFeeTooHigh.setVisibility(View.VISIBLE);
                mBinding.priorityFeeTooHigh.setVisibility(View.VISIBLE);
                mBinding.feeEstimatedValue.setTextColor(Color.RED);
                mBinding.feeMaxValue.setTextColor(Color.RED);
                SpannableStringBuilder spannableMaxEstimated = new SpannableStringBuilder(feeEstimatedContent);
                spannableMaxEstimated.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.red)), 18,
                        18 + transaction.getMaxPriorityFeePerGas().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                SpannableStringBuilder spannableMaxFee = new SpannableStringBuilder(feeMaxContent);
                spannableMaxFee.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.red)), 9,
                        9 + transaction.getMaxPriorityFeePerGas().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mBinding.feeEstimatedDetail.setText(spannableMaxEstimated);
                mBinding.feeMaxDetail.setText(spannableMaxFee);
            } else {
                mBinding.feeEstimatedDetail.setText(feeEstimatedContent);
                mBinding.feeMaxDetail.setText(feeMaxContent);
            }
        }

        if (transaction.getAbi() != null) {
            updateAbiView(transaction);
            mBinding.data.setVisibility(View.VISIBLE);
            mBinding.undecodedData.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(transaction.getMemo())) {
                mBinding.data.setVisibility(View.GONE);
                mBinding.undecodedData.setVisibility(View.VISIBLE);
                mBinding.inputData.setText("0x" + transaction.getMemo());
                if (!TextUtils.isEmpty(transaction.getSelectorMethodName())) {
                    mBinding.tvSelector.setText(transaction.getSelectorMethodName());
                    mBinding.llSelector.setVisibility(View.VISIBLE);
                }
                showDialog();
            } else {
                mBinding.data.setVisibility(View.GONE);
                mBinding.undecodedData.setVisibility(View.GONE);
            }
        }

        mBinding.setTx(transaction);
        processAndUpdateTo(transaction);
        if (transaction.getSignatureUR() != null) {
            mBinding.qrcodeContainer.setVisibility(View.VISIBLE);
            mBinding.qrcode.qrcode.setData(transaction.getSignatureUR());
        }
    }

    private void processAndUpdateTo(EthereumTransaction transaction) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String to = transaction.getTo();
            String ens = EthereumTxViewModel.loadEnsAddress(to);
            String addressSymbol = EthereumTxViewModel.recognizeAddress(transaction, to);
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

    private void updateAbiView(EthereumTransaction transaction) {
        JSONObject abi = transaction.getAbi();
        if (transaction.isFromTFCard()) {
            mBinding.tfcardTip.setVisibility(View.VISIBLE);
        }
        String contract = abi.optString("contract");
        boolean isUniswap = contract.toLowerCase().contains("uniswap");
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AbiItemAdapter.AbiItem> itemList = new AbiItemAdapter(transaction).adapt(abi);
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
                    addViewToData(transaction, isUniswap, itemList);
                }
            });
        });

    }

    private void addViewToData(EthereumTransaction transaction, boolean isUniswap, List<AbiItemAdapter.AbiItem> itemList) {
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
                String ens = EthereumTxViewModel.loadEnsAddress(item.value);
                String addressSymbol = EthereumTxViewModel.recognizeAddress(transaction, item.value);
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
                if (!item.value.equalsIgnoreCase(transaction.getFrom())) {
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
