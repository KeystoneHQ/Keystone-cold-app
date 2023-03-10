package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectOneAddressBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.constant.UIConstants;
import com.keystone.cold.remove_wallet_mode.ui.adapter.ClickAddressAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.AddressItem;
import com.keystone.cold.remove_wallet_mode.ui.views.AddressNumberPicker;
import com.keystone.cold.remove_wallet_mode.viewmodel.AddressViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.NumberPickerCallback;
import com.keystone.cold.ui.modal.ProgressModalDialog;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SelectOneAddressFragment extends BaseFragment<FragmentSelectOneAddressBinding> implements NumberPickerCallback {
    private ClickAddressAdapter clickAddressAdapter;
    private LiveData<List<AddressItem>> addressLiveData;
    private AddressViewModel viewModel;
    private AddressNumberPicker addressNumberPicker;

    @Override
    protected int setView() {
        return R.layout.fragment_select_one_address;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        clickAddressAdapter = new ClickAddressAdapter(mActivity, addr -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, requireArguments().getString(BundleKeys.WALLET_ID_KEY));
            bundle.putSerializable(BundleKeys.ADDRESS_IDS_KEY, (Serializable) Collections.singletonList(addr.getId()));
            bundle.putString(BundleKeys.COIN_ID_KEY, addr.getCoinId());
            navigate(R.id.action_to_syncFragment, bundle);
        });
        Bundle data = requireArguments();
        String coinId = data.getString(BundleKeys.COIN_ID_KEY);
        String pageTitle = data.getString(BundleKeys.PAGE_TITLE_KEY, "");
        if (!pageTitle.isEmpty()) {
            mBinding.toolbarTitle.setText(pageTitle);
        }
        if (!TextUtils.isEmpty(coinId)) {
            AddressViewModel.Factory factory = new AddressViewModel.Factory(mActivity.getApplication(), coinId);
            viewModel = ViewModelProviders.of(this, factory)
                    .get(AddressViewModel.class);
            if (addressLiveData != null) {
                addressLiveData.removeObservers(this);
                addressLiveData = null;
            }
            mBinding.addrList.setAdapter(clickAddressAdapter);
            mBinding.llAddAccounts.setOnClickListener(v -> handleAddAccounts());
            addressLiveData = viewModel.getAddress();
            subscribeUi(addressLiveData);
        }
    }

    private void subscribeUi(LiveData<List<AddressItem>> address) {
        address.observe(this, addressItems -> clickAddressAdapter.setItems(addressItems));
    }

    @Override
    public void onDestroyView() {
        if (addressLiveData != null) {
            addressLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

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

    private void handleAddAccounts() {
        if (addressNumberPicker == null) {
            addressNumberPicker = new AddressNumberPicker();
            addressNumberPicker.setCallback(this);
        }
        addressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
    }
}
