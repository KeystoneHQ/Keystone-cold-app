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
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AddressFragmentBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.util.Keyboard;
import com.keystone.cold.viewmodel.CoinViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.List;
import java.util.stream.Collectors;

public class AddressFragment extends BaseFragment<AddressFragmentBinding> {

    String query;
    private CoinViewModel viewModel;
    private WatchWallet watchWallet;
    private String coinId;
    private LiveData<List<AddressEntity>> addressListLiveData;
    private final AddressCallback mAddrCallback = new AddressCallback() {
        @Override
        public void onClick(AddressEntity addr) {
            if (mAddressAdapter.isEditing()) {
                mAddressAdapter.exitEdit();
            } else {
                Bundle bundle = requireArguments();
                Bundle data = new Bundle();
                data.putString(KEY_COIN_CODE, bundle.getString(KEY_COIN_CODE));
                data.putString(KEY_ADDRESS, addr.getAddressString());
                data.putString(KEY_ADDRESS_NAME, addr.getName());
                data.putString(KEY_ADDRESS_PATH, addr.getPath());
                data.putInt(KEY_ADDRESS_INDEX, addr.getIndex());
                navigate(R.id.action_to_receiveCoinFragment, data);
            }
        }

        @Override
        public void onNameChange(AddressEntity addr) {
            viewModel.updateAddress(addr);
        }
    };

    private AddressAdapter mAddressAdapter;

    public void exitEditAddressName() {
        if (mAddressAdapter.isEditing()) {
            mAddressAdapter.exitEdit();
        }
    }

    public static Fragment newInstance(@NonNull String coinId, @NonNull String coinCode) {
        AddressFragment fragment = new AddressFragment();
        Bundle args = new Bundle();
        args.putString(KEY_COIN_ID, coinId);
        args.putString(KEY_COIN_CODE, coinCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.address_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mAddressAdapter = new AddressAdapter(mActivity, mAddrCallback);
        mBinding.addrList.setAdapter(mAddressAdapter);
        mAddressAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!TextUtils.isEmpty(query) && mAddressAdapter.getItemCount() == 0) {
                    mBinding.empty.setVisibility(View.VISIBLE);
                    mBinding.addrList.setVisibility(View.GONE);
                } else {
                    mBinding.empty.setVisibility(View.GONE);
                    mBinding.addrList.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = requireArguments();
        coinId = data.getString(KEY_COIN_ID);
        requireParentFragment();
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(),
                coinId);
        viewModel = ViewModelProviders.of(getParentFragment(), factory)
                .get(CoinViewModel.class);
        addressListLiveData = viewModel.getNewAddressLiveData(coinId);
        subscribeUi(addressListLiveData);
    }

    private void subscribeUi(LiveData<List<AddressEntity>> address) {
        address.observe(this, addressEntities -> {
            if (watchWallet.equals(WatchWallet.METAMASK)) {
                String code = Utilities.getCurrentEthAccount(mActivity);
                ETHAccount account = ETHAccount.ofCode(code);
                addressEntities = addressEntities.stream()
                        .filter(addressEntity -> isCurrentETHAccountAddress(account, addressEntity))
                        .peek(addressEntity -> {
                            if (addressEntity.getName().startsWith("ETH-")) {
                                addressEntity.setDisplayName(addressEntity.getName().replace("ETH-", "Account "));
                            } else {
                                addressEntity.setDisplayName(addressEntity.getName());
                            }
                        })
                        .collect(Collectors.toList());
            } else if (watchWallet.equals(WatchWallet.SOLANA)) {
                String code = Utilities.getCurrentSolAccount(mActivity);
                SOLAccount account = SOLAccount.ofCode(code);
                addressEntities = addressEntities.stream()
                        .filter(addressEntity -> isCurrentSOLAccountAddress(account, addressEntity))
                        .peek(addressEntity -> {
                            if (addressEntity.getName().startsWith("SOL-")) {
                                addressEntity.setDisplayName(addressEntity.getName().replace("SOL-", "Account "));
                            } else {
                                addressEntity.setDisplayName(addressEntity.getName());
                            }
                        })
                        .collect(Collectors.toList());
            } else {
                addressEntities = addressEntities.stream().peek(addressEntity -> {
                    addressEntity.setDisplayName(addressEntity.getName());
                }).collect(Collectors.toList());
                if (requireArguments().getString(KEY_COIN_CODE).equals(Coins.ETH.coinCode())) {
                    addressEntities = addressEntities.stream().filter(addressEntity -> ETHAccount.isStandardChildren(addressEntity.getPath())).collect(Collectors.toList());
                }
            }
            mAddressAdapter.setItems(addressEntities);
        });
    }

    public static boolean isCurrentETHAccountAddress(ETHAccount account, AddressEntity addressEntity) {
        return account.isChildrenPath(addressEntity.getPath());
    }

    public static boolean isCurrentSOLAccountAddress(SOLAccount account, AddressEntity addressEntity) {
        return account.isChildrenPath(addressEntity.getPath());
    }

    public void setQuery(String s) {
        query = s;
        mAddressAdapter.getFilter().filter(s);
    }

    public void enterSearch() {
        if (mAddressAdapter != null) {
            mAddressAdapter.enterSearch();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Keyboard.hide(mActivity, requireView());
    }

    public void updateAddressList() {
        if (addressListLiveData != null) {
            addressListLiveData.removeObservers(this);
            addressListLiveData = null;
        }
        addressListLiveData = viewModel.getNewAddressLiveData(coinId);
        subscribeUi(addressListLiveData);
    }
}
