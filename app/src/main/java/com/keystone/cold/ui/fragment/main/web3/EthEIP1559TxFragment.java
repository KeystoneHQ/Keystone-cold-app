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

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.keystone.cold.ui.fragment.main.web3.EthTxConfirmFragment.highLight;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.ETH.GnosisHandler;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AbiItemBinding;
import com.keystone.cold.databinding.AbiItemMethodBinding;
import com.keystone.cold.databinding.EnsItemBinding;
import com.keystone.cold.databinding.EthEip1559TxBinding;
import com.keystone.cold.db.entity.GenericETHTxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.tx.Web3TxViewModel;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class EthEIP1559TxFragment extends BaseFragment<EthEip1559TxBinding> {

    private GenericETHTxEntity genericETHTxEntity;
    private Web3TxViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.eth_eip1559_tx;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot));
        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadEIP1559ETHTx(bundle.getString(KEY_TX_ID)).observe(this, genericETHTxEntity -> {
            this.genericETHTxEntity = genericETHTxEntity;
            if (this.genericETHTxEntity != null) {
                updateUI();
            }
        });
        viewModel = ViewModelProviders.of(this).get(Web3TxViewModel.class);
        mBinding.ethTx.info.setOnClickListener(view1 -> showDialog());
    }

    private void showDialog() {
        ModalDialog.showCommonModal((AppCompatActivity) getActivity(),
                getString(R.string.tip),
                getString(R.string.learn_more),
                getString(R.string.know),
                null);
    }

    private void updateUI() {
        JSONObject abi = null;
        try {
            abi = new JSONObject(genericETHTxEntity.getMemo());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.ethTx.network.setText(viewModel.getNetwork(genericETHTxEntity.getChainId()));
        showQrCode();
        updateAbiView(abi);
        mBinding.setTx(genericETHTxEntity);
        processAndUpdateTo();
    }

    private void showQrCode() {
        try {
            byte[] signature = Hex.decode(genericETHTxEntity.getSignature());
            UUID uuid = UUID.fromString(genericETHTxEntity.getSignId());
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            EthSignature ethSignature = new EthSignature(signature, requestId);
            mBinding.qrcode.qrcode.setData(ethSignature.toUR().toString());
        } catch (Exception e) {
            mBinding.qrcode.qrcode.setData(Hex.toHexString(genericETHTxEntity.getSignature().getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void updateAbiView(JSONObject abi) {
        JSONObject signData = null;
        try {
            signData = new JSONObject(genericETHTxEntity.getSignedHex());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (abi != null) {
            String contract = abi.optString("contract");
            boolean isUniswap = contract.toLowerCase().contains("uniswap");
            List<AbiItemAdapter.AbiItem> itemList = new AbiItemAdapter(genericETHTxEntity.getFrom(), viewModel).adapt(abi);
            addViewToData(isUniswap, itemList);
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
            } else {
                mBinding.ethTx.data.setVisibility(View.GONE);
                mBinding.ethTx.undecodedData.setVisibility(View.GONE);
            }
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

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}