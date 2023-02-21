package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.keystone;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Arith;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentKeystoneTxBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.BindingAdapters;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.TransactionItem;
import com.keystone.cold.ui.fragment.main.TransactionItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class KeystoneTransactionDetailFragment extends BaseFragment<FragmentKeystoneTxBinding> {
    private final MutableLiveData<TxEntity> transaction;

    public KeystoneTransactionDetailFragment(MutableLiveData<TxEntity> transaction) {
        this.transaction = transaction;
    }

    public static KeystoneTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<TxEntity> transaction) {
        KeystoneTransactionDetailFragment fragment = new KeystoneTransactionDetailFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_keystone_tx;
    }

    @Override
    protected void init(View view) {
        transaction.observe(this, (txEntity) -> {
            if (txEntity != null) {
                mBinding.setTx(txEntity);
                if (Coins.isBTCFamily(txEntity.getCoinCode())) {
                    mBinding.txDetail.fromRow.setVisibility(View.GONE);
                    mBinding.txDetail.arrowDown.setVisibility(View.GONE);
                }
                refreshAmount(txEntity);
                refreshFromList(txEntity);
                refreshReceiveList(txEntity);
                refreshTokenUI(txEntity);
                refreshFeeDisplay(txEntity);
                refreshMemoDisplay(txEntity);
                if (txEntity.getSignResult() != null) {
                    mBinding.qrcodeContainer.setVisibility(View.VISIBLE);
                    mBinding.qrcode.qrcode.setData(txEntity.getSignResult());
                }
            }
        });
    }

    private void refreshMemoDisplay(TxEntity txEntity) {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.memoLabel.setText(R.string.tag);
        } else if (Coins.isPolkadotFamily(txEntity.getCoinCode()) || txEntity.getCoinCode().equals(Coins.CFX.coinCode())) {
            mBinding.txDetail.memoInfo.setVisibility(View.GONE);
        }
    }

    private void refreshFeeDisplay(TxEntity txEntity) {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.feeInfo.setVisibility(View.GONE);
        } else if (Coins.isPolkadotFamily(txEntity.getCoinCode())) {
            mBinding.txDetail.feeLabel.setText(R.string.dot_tip);
        }
        checkBtcFee(txEntity);
    }

    private void checkBtcFee(TxEntity txEntity) {
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

    private void refreshAmount(TxEntity txEntity) {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshTokenUI(TxEntity txEntity) {
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

    private void refreshReceiveList(TxEntity txEntity) {
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

    private void refreshFromList(TxEntity txEntity) {
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
