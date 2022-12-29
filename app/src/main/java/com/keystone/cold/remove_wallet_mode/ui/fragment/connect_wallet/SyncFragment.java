package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.cold.R;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentSyncBinding;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.FewchaWalletViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.Constants;
import com.sparrowwallet.hummingbird.UR;

import java.util.List;
import java.util.Objects;

public class SyncFragment extends BaseFragment<FragmentSyncBinding> {


    private Wallet wallet;
    private List<Long> addressIds;

    @Override
    protected int setView() {
        return R.layout.fragment_sync;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        Bundle data = getArguments();
        String walletId = data.getString(Constants.KEY_WALLET_ID);
        if (data.containsKey(Constants.KEY_ADDRESS_IDS)) {
            addressIds = (List<Long>) data.getSerializable(Constants.KEY_ADDRESS_IDS);
        }
        wallet = Wallet.getWalletById(walletId);
        mBinding.setWallet(wallet.getWalletName());

        mBinding.complete.setOnClickListener(v -> mActivity.finish());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.more, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        binding.rlSelectAddress.setVisibility(View.VISIBLE);
        binding.rlSelectAddress.setOnClickListener(v -> {
            navigateUp();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_WALLET_ID, wallet.getWalletId());
            navigate(R.id.action_to_selectAddressFragment, bundle);
            dialog.dismiss();

        });
        binding.rlTutorial.setVisibility(View.VISIBLE);
        binding.rlTutorial.setOnClickListener(v -> {

            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }



}
