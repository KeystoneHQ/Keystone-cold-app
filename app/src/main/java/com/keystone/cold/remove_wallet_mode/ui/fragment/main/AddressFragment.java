package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.keystone.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentAddressListBinding;
import com.keystone.cold.remove_wallet_mode.ui.adapter.AddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.ui.model.AssetItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.AssetViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.List;
import java.util.Optional;

public class AddressFragment extends BaseFragment<FragmentAddressListBinding> {

    private AddressViewModel viewModel;
    private LiveData<List<AddressItem>> addressLiveData;
    private AddressAdapter addressAdapter;


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
                } else {
                    Bundle bundle = requireArguments();
                    Bundle data = new Bundle();
                    data.putString(KEY_COIN_CODE, bundle.getString(KEY_COIN_CODE));
                    data.putString(KEY_ADDRESS, addr.getAddress());
                    data.putString(KEY_ADDRESS_NAME, addr.getName());
                    data.putString(KEY_ADDRESS_PATH, addr.getPath());
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
        Bundle data = requireArguments();
        String coinId = data.getString(KEY_COIN_ID);

        AssetViewModel assetViewModel = ViewModelProviders.of(this).get(AssetViewModel.class);

        assetViewModel.loadAssets().observe(this, assets -> {
            if (assets == null) return;
            Optional<AssetItem> assetItem = assets.stream().filter(asset -> asset.getCoinId().equals(coinId)).findFirst();
            if (assetItem.isPresent()) {
                String canonicalCoinId = assetItem.get().getCanonicalCoinIdByEcology();
                AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), canonicalCoinId);
                viewModel = ViewModelProviders.of(getParentFragment(), factory)
                        .get(AddressViewModel.class);
                addressLiveData = viewModel.getAddress();
                subscribeUi(addressLiveData);
            } else {
                ModalDialog.showCommonModal(mActivity, "Invalid State", "Cannot find coin " + coinId, "Back", this::navigateUp);
            }
        });
    }

    private void subscribeUi(LiveData<List<AddressItem>> address) {
        address.observe(this, addressItems -> {
            Log.d("sora", "subscribeUi: " + addressItems);
            addressAdapter.setItems(addressItems);
        });
    }

    public void exitEditAddressName() {
        addressAdapter.exitEdit();
    }

    @Override
    public void onDestroyView() {
        if (addressLiveData != null) {
            addressLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }
}