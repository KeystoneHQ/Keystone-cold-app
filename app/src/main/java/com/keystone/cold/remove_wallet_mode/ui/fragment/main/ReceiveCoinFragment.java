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

package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentReceiveBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.Objects;

public class ReceiveCoinFragment extends BaseFragment<FragmentReceiveBinding> {


    @Override
    protected int setView() {
        return R.layout.fragment_receive;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        String coinCode = data.getString(KEY_COIN_CODE);
        if(coinCode.startsWith("BTC")) {
            mBinding.setDisplayName("BTC");
        } else {
            mBinding.setDisplayName(coinCode);
        }
        setWarningInfo(coinCode);
        mBinding.setCoinCode(coinCode);
        String address = data.getString(KEY_ADDRESS);
        mBinding.setAddress(address);
        mBinding.setAddressName(data.getString(KEY_ADDRESS_NAME));
        mBinding.setPath(data.getString(KEY_ADDRESS_PATH));
        mBinding.qrcode.setData(address);
    }

    private void setWarningInfo(String coinCode) {
        if(coinCode.startsWith("BTC")) {
            mBinding.setHintInfo("BTC");
        } else {
            String info = coinCode;
            switch (coinCode.toUpperCase()) {
                case "ETH":
                    info = getString(R.string.eth_receive_info);
                    break;
                case "SOL":
                    info = getString(R.string.sol_receive_info);
                    break;
                case "TRON":
                    info = getString(R.string.tron_receive_info);
                    break;
                case "APTOS":
                    info = getString(R.string.aptos_receive_info);
                    break;
                case "AVAX":
                    info = getString(R.string.avax_receive_info);
            }
            mBinding.setHintInfo(info);
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }


}
