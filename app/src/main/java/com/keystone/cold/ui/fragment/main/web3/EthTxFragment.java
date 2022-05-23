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

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.GnosisHandler;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AbiItemBinding;
import com.keystone.cold.databinding.AbiItemMethodBinding;
import com.keystone.cold.databinding.EnsItemBinding;
import com.keystone.cold.databinding.EthTxBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.keystone.cold.ui.fragment.main.web3.EthTxConfirmFragment.MAX_PER_GAS;
import static com.keystone.cold.ui.fragment.main.web3.EthTxConfirmFragment.highLight;

public class EthTxFragment extends BaseFragment<EthTxBinding> {

    private TxEntity txEntity;
    private Web3TxViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.eth_tx;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot));
        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                updateUI();
            }
        });
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        mBinding.ethTx.info.setOnClickListener(view1 -> showDialog());
    }

    private void showDialog() {
        ModalDialog.showCommonModal((AppCompatActivity) getActivity(),
                getString(R.string.tip),
                getString(R.string.learn_more_doc),
                getString(R.string.know),
                null);
    }

    private void updateUI() {
        int chainId = 1;
        JSONObject signed = null;
        JSONObject abi = null;
        try {
            signed = new JSONObject(txEntity.getSignedHex());
            chainId = signed.optInt("chainId", 1);
            abi = signed.getJSONObject("abi");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.ethTx.network.setText(viewModel.getNetwork(chainId));
        showQrCode(signed);
        updateAbiView(abi);
        mBinding.ethTx.setTx(txEntity);
        processAndUpdateTo();
    }

    private void showQrCode(JSONObject signed) {
        try {
            WatchWallet watchWallet = WatchWallet.getWatchWallet(mActivity);
            if (watchWallet.equals(WatchWallet.METAMASK)) {
                try {
                    if (signed != null) {
                        signed.remove("abi");
                        signed.remove("chainId");
                        byte[] signature = Hex.decode(signed.getString("signature"));
                        UUID uuid = UUID.fromString(signed.getString("signId"));
                        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
                        byteBuffer.putLong(uuid.getMostSignificantBits());
                        byteBuffer.putLong(uuid.getLeastSignificantBits());
                        byte[] requestId = byteBuffer.array();
                        EthSignature ethSignature = new EthSignature(signature, requestId);
                        mBinding.qrcode.qrcode.setData(ethSignature.toUR().toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (signed != null) {
                    signed.remove("abi");
                    signed.remove("chainId");
                    mBinding.qrcode.qrcode.setData(Hex.toHexString(signed.toString().getBytes(StandardCharsets.UTF_8)));
                }
            }
        } catch (Exception e) {
            signed.remove("abi");
            signed.remove("chainId");
            mBinding.qrcode.qrcode.setData(Hex.toHexString(signed.toString().getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void updateAbiView(JSONObject abi) {
        JSONObject signData = null;
        try {
            signData = new JSONObject(txEntity.getSignedHex());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (abi != null) {
            String contract = abi.optString("contract");
            boolean isUniswap = contract.toLowerCase().contains("uniswap");
            List<AbiItemAdapter.AbiItem> itemList = new AbiItemAdapter(txEntity.getFrom(), viewModel).adapt(abi);
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
            mBinding.ethTx.data.setVisibility(View.VISIBLE);
            mBinding.ethTx.undecodedData.setVisibility(View.GONE);
            if (signData != null) {
                if (signData.optBoolean("isFromTFCard")) {
                    mBinding.ethTx.tfcardTip.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (signData != null && !TextUtils.isEmpty(signData.optString("inputData"))) {
                mBinding.ethTx.data.setVisibility(View.GONE);
                mBinding.ethTx.undecodedData.setVisibility(View.VISIBLE);
                mBinding.ethTx.inputData.setText("0x" + signData.optString("inputData"));

                String selector = viewModel.recognizedSelector(signData.optString("inputData"));
                if (!TextUtils.isEmpty(selector)) {
                    updateSelectorView(selector);
                }
            } else {
                mBinding.ethTx.data.setVisibility(View.GONE);
                mBinding.ethTx.undecodedData.setVisibility(View.GONE);
            }
        }
    }

    private void updateSelectorView(String selector) {
        mBinding.ethTx.tvSelector.setText(selector);
        mBinding.ethTx.llSelector.setVisibility(View.VISIBLE);
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
                if (!item.value.equalsIgnoreCase(txEntity.getFrom())) {
                    item.value += String.format(" [%s]", getString(R.string.inconsistent_address));
                }
            }
            binding.value.setText(highLight(item.value));
            mBinding.ethTx.container.addView(binding.getRoot());
        }
    }

    private void processAndUpdateTo() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String to = txEntity.getTo();
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
