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

package com.keystone.cold.ui.fragment.main.polkadot;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.PolkadotTxBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinListViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import static com.keystone.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.keystone.cold.viewmodel.WatchWallet.POLKADOT_JS;

public class PolkadotTxFragment extends BaseFragment<PolkadotTxBinding> {
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.polkadot_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle bundle = requireArguments();
        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            if (this.txEntity != null) {
                mBinding.dotTx.txDetail.updateUI(txEntity);
                mBinding.dotTx.qrcode.qrcode.disableMultipart();
                mBinding.dotTx.qrcode.qrcode.setVisibility(View.VISIBLE);
                mBinding.dotTx.broadcastHint.setVisibility(View.VISIBLE);
                mBinding.dotTx.broadcastHint.setText(getString(R.string.please_broadcast_with_hot));
                try {
                    mBinding.dotTx.qrcode.qrcode.setData(new JSONObject(txEntity.getSignedHex())
                            .getString("signedHex"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
