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
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_INDEX;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentReceiveBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

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
        if (coinCode.startsWith("BTC")) {
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
        if (coinCode.equals(Coins.ADA.coinCode())) {
            setupCardano();
        }
    }

    private void setupCardano() {
        CardanoViewModel cardanoViewModel = ViewModelProviders.of(this).get(CardanoViewModel.class);

        Bundle data = requireArguments();
        String address = data.getString(KEY_ADDRESS);
        String path = data.getString(KEY_ADDRESS_PATH);
        int addressIndex = data.getInt(KEY_ADDRESS_INDEX);

        mBinding.setAddress(shortenAddress(address));
        mBinding.path.setVisibility(View.GONE);
        mBinding.moreDetail.setVisibility(View.VISIBLE);
        mBinding.addressContainer.setOnClickListener((v) -> {
            LiveData<String[]> addresses = cardanoViewModel.getStakeAddress(Utilities.getCurrentCardanoAccount(mActivity), addressIndex);
            addresses.observe(this, (value) -> {
                if (value != null) {
                    String stakeAddress = value[0];
                    String enterpriseAddress = value[1];
                    ModalDialog.showCardanoAddressDetailModal(mActivity, path, address, enterpriseAddress, stakeAddress);
                    addresses.removeObservers(this);
                }
            });
        });
    }

    private String shortenAddress(String address) {
        String result = "";
        int length = address.length();
        if (length > 36) {
            result += address.substring(0, 16);
            result += "....";
            result += address.substring(length - 16, length);
            return result;
        } else {
            return address;
        }
    }

    private void setWarningInfo(String coinCode) {
        if (coinCode.startsWith("BTC")) {
            mBinding.setHintInfo("BTC");
        } else {
            String info = coinCode + " " + getString(R.string.general_receive_info);
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
