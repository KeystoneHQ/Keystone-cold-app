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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.registry.EthSignature;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EthBroadcastTxFragment extends BroadcastTxFragment {
    public static final String KEY_SIGNATURE_JSON = "SignatureJson";
    private String messageSignature;

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
        String txId = data.getString(KEY_TXID);
        messageSignature = data.getString(KEY_SIGNATURE_JSON);
        mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());
        if (!TextUtils.isEmpty(txId)) {
            ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                    .loadETHTx(data.getString(KEY_TXID)).observe(this, genericETHTxEntity -> {
                if (genericETHTxEntity != null) {
                    mBinding.setCoinCode(genericETHTxEntity.getCoinCode());
                    this.txEntity = genericETHTxEntity;
                    refreshUI();
                }
            });
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        mBinding.broadcastHint.setText(R.string.sync_with_metamask);
        mBinding.icon.setImageDrawable(mActivity.getDrawable(R.drawable.coin_eth));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public String getSignedTxData() {
        try {
            JSONObject messageSignatureJson = new JSONObject(messageSignature);
            byte[] signature = Hex.decode(messageSignatureJson.getString("signature"));
            UUID uuid = UUID.fromString(messageSignatureJson.getString("requestId"));
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            byte[] requestId = byteBuffer.array();
            EthSignature ethSignature = new EthSignature(signature, requestId);
            return ethSignature.toUR().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
