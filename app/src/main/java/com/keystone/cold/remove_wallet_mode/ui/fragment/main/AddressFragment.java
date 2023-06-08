package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_INDEX;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentAddressListBinding;
import com.keystone.cold.remove_wallet_mode.ui.adapter.AddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.AssetViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.List;
import java.util.Optional;

public class AddressFragment extends BaseFragment<FragmentAddressListBinding> {

    private AddressViewModel viewModel;
    private AddressAdapter addressAdapter;
    private LiveData<List<AddressItem>> addressItems;

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
        return R.layout.fragment_address_list;
    }

    @Override
    protected void init(View view) {
        addressAdapter = new AddressAdapter(mActivity, new AddressAdapter.AddressCallback() {
            @Override
            public void onClick(AddressItem addr) {
                if (addressAdapter.isEditing()) {
                    addressAdapter.exitEdit();
                    return;
                }
                Bundle bundle = requireArguments();
                Bundle data = new Bundle();
                data.putString(KEY_COIN_CODE, bundle.getString(KEY_COIN_CODE));
                data.putString(KEY_ADDRESS, addr.getAddress());
                data.putString(KEY_ADDRESS_NAME, addr.getName());
                data.putString(KEY_ADDRESS_PATH, addr.getPath());
                data.putInt(KEY_ADDRESS_INDEX, addr.getIndex());
                if (Coins.NEAR.coinCode().equals(bundle.getString(KEY_COIN_CODE))) {
                    navigate(R.id.action_to_nearAccountInformationFragment, data);
                } else {
                    navigate(R.id.action_to_receiveCoinFragment, data);
                }
            }

            @Override
            public void onNameChange(AddressItem addr) {
                viewModel.updateAddress(addr);
            }
        });

        mBinding.addrList.setAdapter(addressAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        initAddresses();
    }

    private void subscribeUi(LiveData<List<AddressItem>> address) {
        address.observe(this, addressItems -> {
            addressAdapter.setItems(addressItems);
        });
    }

    public void exitEditAddressName() {
        addressAdapter.exitEdit();
    }

    public void initAddresses() {
        Bundle data = requireArguments();
        String coinId = data.getString(KEY_COIN_ID);
        AssetViewModel assetViewModel = ViewModelProviders.of(this).get(AssetViewModel.class);

        if (coinId.equals(Coins.ADA.coinId())) {
            CardanoViewModel cardanoViewModel = ViewModelProviders.of(this).get(CardanoViewModel.class);
            int accountIndex = Utilities.getCurrentCardanoAccount(mActivity);
            cardanoViewModel.checkAddressOrAdd(accountIndex);
        }

        assetViewModel.loadAssets().observe(this, assets -> {
            if (assets == null) return;
            BTCAccount btcAccount = null;
            if (coinId.equals(Coins.BTC.coinId())) {
                btcAccount = BTCAccount.ofCode(Utilities.getCurrentBTCAccount(mActivity));
            }
            Optional<AssetItem> assetItem = assets.stream().filter(asset -> asset.getCoinId().equals(coinId)).findFirst();
            if (assetItem.isPresent()) {
                String canonicalCoinId = assetItem.get().getCanonicalCoinIdByEcology();
                if (btcAccount != null) {
                    canonicalCoinId = btcAccount.getCoinId();
                }
                AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), canonicalCoinId);
                viewModel = ViewModelProviders.of(getParentFragment(), factory)
                        .get(AddressViewModel.class);
                viewModel.updateCoinId(canonicalCoinId);
                if (addressItems != null) {
                    addressItems.removeObservers(this);
                    addressItems = null;
                }
                addressItems = viewModel.getAddress(assetItem.get());
                if (canonicalCoinId.equals(Coins.ADA.coinId())) {
                    addressItems = CardanoViewModel.filterAddressByAccount(addressItems, Utilities.getCurrentCardanoAccount(mActivity));
                }
                subscribeUi(addressItems);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (addressItems != null) {
            addressItems.removeObservers(this);
        }
        super.onDestroyView();
    }
}
