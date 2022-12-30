package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectAddressBinding;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.ui.adapter.SelectedAddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.SelectAddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.Constants;
import com.keystone.cold.ui.fragment.main.AddressNumberPicker;
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
        mBinding.ivConfirm.setOnClickListener((View.OnClickListener) v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_WALLET_ID, requireArguments().getString(Constants.KEY_WALLET_ID));
            bundle.putSerializable(Constants.KEY_ADDRESS_IDS, (Serializable) selectedAddressAdapter.getAddressIds());
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
        String walletId = data.getString(Constants.KEY_WALLET_ID);
        SelectAddressViewModel selectAddressViewModel = ViewModelProviders.of(this).get(SelectAddressViewModel.class);
        LiveData<String> coinIdLiveData = selectAddressViewModel.getCoinId(walletId);
        coinIdLiveData.observe(this, coinId -> {
            showAddressList(coinId);
            coinIdLiveData.removeObservers(this);
        });
    }

    private void showAddressList(String coinId) {
        AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), coinId);
        viewModel = ViewModelProviders.of(this, factory)
                .get(AddressViewModel.class);
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
