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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.BroadcastTxFragmentBinding;
import com.keystone.cold.model.Tx;
import com.keystone.cold.protocol.builder.SignTxResultBuilder;
import com.keystone.cold.ui.BindingAdapters;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;

public class BroadcastTxFragment extends BaseFragment<BroadcastTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    public static final String KEY_SIGNATURE_UR = "signature_ur";

    protected WatchWallet watchWallet;

    protected Tx txEntity;

    protected final View.OnClickListener goHome = v -> navigate(R.id.action_to_home);

    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);

        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            mBinding.setCoinCode(txEntity.getCoinCode());
            this.txEntity = txEntity;
            refreshUI();
            mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());
        });
    }

    protected void refreshUI() {
        mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot));
        if (watchWallet == WatchWallet.POLKADOT_JS) {
            mBinding.qrcodeLayout.qrcode.disableMultipart();
        }
        refreshTokenUI();
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
        if(txEntity.getCoinCode().startsWith("BTC")) {
            BindingAdapters.setIcon(mBinding.icon,
                    txEntity.getCoinCode());
        }
        else {
            BindingAdapters.setIcon(mBinding.icon,
                    txEntity.getCoinCode(),
                    assetCode);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    public String getSignedTxData() {
        if (watchWallet == WatchWallet.KEYSTONE) {
            return getSignTxJson(txEntity);
        } else if (watchWallet == WatchWallet.POLKADOT_JS) {
            try {
                return new JSONObject(txEntity.getSignedHex())
                        .getString("signedHex");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    protected String getSignTxJson(Tx txEntity) {
        SignTxResultBuilder signTxResult = new SignTxResultBuilder();
        signTxResult.setRawTx(txEntity.getSignedHex())
                .setSignId(txEntity.getSignId())
                .setTxId(txEntity.getTxId());
        return signTxResult.build();
    }
}
