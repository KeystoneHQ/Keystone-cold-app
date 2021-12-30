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

package com.keystone.cold.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_INDEX;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.cold.R;
import com.keystone.cold.databinding.ReceiveFragmentBinding;
import com.keystone.cold.databinding.XrpSyncMenuBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.Objects;

public class ReceiveCoinFragment extends BaseFragment<ReceiveFragmentBinding> {
    private int index;

    @Override
    protected int setView() {
        return R.layout.receive_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        String coinCode = data.getString(KEY_COIN_CODE);
        if(coinCode.startsWith("BTC")) {
            mBinding.setDisplayName("BTC");
        }
        else {
            mBinding.setDisplayName(coinCode);
        }
        mBinding.setCoinCode(coinCode);
        String address = data.getString(KEY_ADDRESS);
        index = data.getInt(KEY_ADDRESS_INDEX);
        mBinding.setAddress(address);
        mBinding.setAddressName(data.getString(KEY_ADDRESS_NAME));
        mBinding.setPath(data.getString(KEY_ADDRESS_PATH));
        mBinding.qrcode.setData(address);
        setupMenu();
    }

    private void setupMenu() {
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT) {
            mBinding.toolbar.inflateMenu(R.menu.more);
            mBinding.toolbar.setOnMenuItemClickListener(item -> {
                showBottomSheetMenu();
                return true;
            });
        }
    }

    public void syncXrp() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        navigate(R.id.action_to_syncFragment, bundle);
    }

    private void syncSparkToken() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        navigate(R.id.action_to_sparkTokenClaimGuide, bundle);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        XrpSyncMenuBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.xrp_sync_menu, null, false);
        binding.pairApp.setOnClickListener(v -> {
            syncXrp();
            dialog.dismiss();

        });
//        binding.sparkTokenClaim.setOnClickListener(v -> {
//            syncSparkToken();
//            dialog.dismiss();
//        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

}
