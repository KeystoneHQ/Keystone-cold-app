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

package com.keystone.cold.ui.fragment.main.xumm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.coins.XRP.SupportTransactions;
import com.keystone.coinlib.coins.XRP.XrpTransaction;
import com.keystone.coinlib.exception.InvalidAccountException;
import com.keystone.cold.R;
import com.keystone.cold.databinding.XrpTxDetailBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.tx.XummTxConfirmViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class XummTxDetailFragment extends BaseFragment<XrpTxDetailBinding> {

    private static final String XRP_TX = "xrp_tx";
    private static final String XRP_TX_HEX = "xrp_tx_hex";
    private XummTxConfirmViewModel viewModel;

    static Fragment newInstance(@NonNull String json, String txHex) {
        XummTxDetailFragment fragment = new XummTxDetailFragment();
        Bundle args = new Bundle();
        args.putString(XRP_TX, json);
        args.putString(XRP_TX_HEX, txHex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.xrp_tx_detail;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();
        try {
            JSONObject xrpTx = new JSONObject(bundle.getString(XRP_TX));
            String txHex = bundle.getString(XRP_TX_HEX);
            //unsigned
            if (TextUtils.isEmpty(txHex)) {
                viewModel = ViewModelProviders.of(mActivity).get(XummTxConfirmViewModel.class);
                viewModel.parseTxException().observe(this, this::handleParseException);
                viewModel.parseXummTxData(xrpTx);
                viewModel.getDisplayJson().observe(this, tx -> mBinding.container.setData(tx));
                mBinding.qr.setVisibility(View.GONE);
            } else {
                //signed
                XrpTransaction xrpTransaction = SupportTransactions.get(xrpTx.getString("TransactionType"));
                if (xrpTransaction != null) {
                    mBinding.container.setData(xrpTransaction.flatTransactionDetail(xrpTx));
                }
                mBinding.qr.setVisibility(View.VISIBLE);
                mBinding.qrcode.qrcode.setData(txHex);
                mBinding.broadcastHint.setText(getString(R.string.broadcast_xrp_toolkit_hint));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            if (ex instanceof InvalidAccountException) {
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.account_not_match),
                        getString(R.string.account_not_match_detail) ,
                        getString(R.string.confirm),
                        null);
            } else {
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.invalid_data),
                        getString(R.string.incorrect_tx_data),
                        getString(R.string.confirm),
                        null);
            }
            viewModel.parseTxException().setValue(null);
            popBackStack(R.id.assetFragment, false);
        }
    }

}
