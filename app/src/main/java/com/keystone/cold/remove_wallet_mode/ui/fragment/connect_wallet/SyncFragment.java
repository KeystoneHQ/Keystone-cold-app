package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSyncBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.FewchaWalletViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.Constants;
import com.sparrowwallet.hummingbird.UR;

import java.util.List;

public class SyncFragment extends BaseFragment<FragmentSyncBinding> {


    private Wallet wallet;
    private List<Long> addressIds;

    @Override
    protected int setView() {
        return R.layout.fragment_sync;
    }

    @Override
    protected void init(View view) {
        Bundle data = getArguments();
        String walletId = data.getString(Constants.KEY_WALLET_ID);
        if (data.containsKey(Constants.KEY_ADDRESS_IDS)) {
            addressIds = (List<Long>) data.getSerializable(Constants.KEY_ADDRESS_IDS);
        }
        wallet = Wallet.getWalletById(walletId);
        mBinding.setWallet(wallet.getWalletName());

        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.complete.setOnClickListener(v -> mActivity.finish());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        generateSyncData();
    }

    private void generateSyncData() {
        MutableLiveData<UR> urMutableLiveData;
        switch (wallet) {
            case FEWCHA:
                FewchaWalletViewModel fewchaWalletViewModel = ViewModelProviders.of(this).get(FewchaWalletViewModel.class);
                fewchaWalletViewModel.setAddressIds(addressIds);
                urMutableLiveData = fewchaWalletViewModel.generateSyncUR();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + wallet);
        }
        if (urMutableLiveData != null) {
            urMutableLiveData.observe(this, ur -> {
                if (ur != null) {
                    mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                }
                urMutableLiveData.removeObservers(this);
            });
        }
    }
}
