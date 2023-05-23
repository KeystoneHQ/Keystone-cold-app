package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectAddressBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.ui.adapter.SelectedAddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.ui.views.AddressNumberPicker;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;
import com.keystone.cold.ui.modal.ProgressModalDialog;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SelectAddressFragment extends BaseFragment<FragmentSelectAddressBinding> implements NumberPickerCallback {


    private AddressViewModel viewModel;
    private SelectedAddressAdapter selectedAddressAdapter;
    private LiveData<List<AddressItem>> addressLiveData;
    private AddressNumberPicker addressNumberPicker;

    @Override
    protected int setView() {
        return R.layout.fragment_select_address;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        if (selectedAddressAdapter == null) {
            mBinding.ivConfirm.setEnabled(false);
            selectedAddressAdapter = new SelectedAddressAdapter(mActivity, (addr, position) -> {
                selectedAddressAdapter.toggleChecked(position);
                mBinding.ivConfirm.setEnabled(selectedAddressAdapter.existSelectedAddress());
            });
        }
        mBinding.addrList.setAdapter(selectedAddressAdapter);
        mBinding.llAddAccounts.setOnClickListener(v -> handleAddAccounts());
        mBinding.ivConfirm.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, requireArguments().getString(BundleKeys.WALLET_ID_KEY));
            bundle.putSerializable(BundleKeys.ADDRESS_IDS_KEY, (Serializable) selectedAddressAdapter.getAddressIds());
            bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY));
            navigate(R.id.action_to_syncFragment, bundle);
        });
    }

    private void handleAddAccounts() {
        if (addressNumberPicker == null) {
            addressNumberPicker = new AddressNumberPicker();
            addressNumberPicker.setCallback(this);
        }
        addressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = requireArguments();
        String coinId = data.getString(BundleKeys.COIN_ID_KEY);
        showAddressList(coinId);
    }

    private void showAddressList(String coinId) {
        AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), coinId);
        viewModel = ViewModelProviders.of(this, factory)
                .get(AddressViewModel.class);
        if (addressLiveData != null) {
            addressLiveData.removeObservers(this);
            addressLiveData = null;
        }
        addressLiveData = viewModel.getAddress();
        subscribeUi(addressLiveData);
    }

    private void subscribeUi(LiveData<List<AddressItem>> address) {
        address.observe(this, addressItems -> {
            selectedAddressAdapter.setItems(addressItems);
            mBinding.ivConfirm.setEnabled(selectedAddressAdapter.existSelectedAddress());
        });
    }


    @Override
    public void onDestroyView() {
        if (addressLiveData != null) {
            addressLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onValueSet(int value) {
        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler(MainApplication.getApplication().getMainLooper());
        Runnable runnable = () -> handler.postDelayed(dialog::dismiss, UIConstants.DIALOG_DISMISS_DELAY_TIME);
        LiveData<Boolean> complete = viewModel.addAddress(value);
        complete.observe(this, result -> {
            runnable.run();
            complete.removeObservers(this);
        });
    }
}
