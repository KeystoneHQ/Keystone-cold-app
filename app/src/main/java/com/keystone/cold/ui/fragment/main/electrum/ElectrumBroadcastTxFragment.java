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

package com.keystone.cold.ui.fragment.main.electrum;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Base43;
import com.keystone.cold.R;
import com.keystone.cold.databinding.BroadcastElectrumTxFragmentBinding;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.db.entity.TxEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.CoinListViewModel;

import org.spongycastle.util.encoders.Hex;

import static com.keystone.cold.ui.fragment.main.electrum.ElectrumTxConfirmFragment.showExportTxnDialog;

public class ElectrumBroadcastTxFragment extends BaseFragment<BroadcastElectrumTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    private final View.OnClickListener goHome = v -> navigate(R.id.action_to_home);
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.broadcast_electrum_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            mBinding.setCoinCode(txEntity.getCoinCode());
            String txString = getSignTxString(txEntity);
            mBinding.qrcodeLayout.qrcode.setData(txString);
        });
        mBinding.hint.setOnClickListener(v -> {
            if (txEntity != null) {
                showExportTxnDialog(mActivity, txEntity.getTxId(), txEntity.getSignedHex(),null);
            }
        });
        mBinding.info.setOnClickListener(v -> showElectrumInfo(mActivity));
    }

    static void showElectrumInfo(AppCompatActivity activity) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.electrum_broadcast_guide);
        binding.subTitle.setText(R.string.electrum_broadcast_action_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getSignTxString(TxEntity txEntity) {
        return Base43.encode(Hex.decode(txEntity.getSignedHex()));
    }
}
