package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentAddressListBinding;
import com.keystone.cold.remove_wallet_mode.ui.adapter.AddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.List;

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
        AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), coinId);
        viewModel = ViewModelProviders.of(getParentFragment(), factory)
                .get(AddressViewModel.class);
        addressLiveData = viewModel.getAddress();
        subscribeUi(addressLiveData);
    }

    private void subscribeUi(LiveData<List<AddressItem>> address) {
        address.observe(this, addressItems -> addressAdapter.setItems(addressItems));
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
