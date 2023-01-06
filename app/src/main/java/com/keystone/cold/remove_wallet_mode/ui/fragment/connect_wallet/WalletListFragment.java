package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentWalletListBinding;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.remove_wallet_mode.helper.SyncMode;
import com.keystone.cold.remove_wallet_mode.ui.adapter.WalletListAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.WalletViewModel;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.Constants;

import java.util.List;

public class WalletListFragment extends BaseFragment<FragmentWalletListBinding> {

    private LiveData<List<WalletItem>> walletLiveData;

    private WalletListAdapter walletListAdapter;
    private WalletViewModel walletViewModel;

    @Override
    protected int setView() {
        return R.layout.fragment_wallet_list;
    }

    @Override
    protected void init(View view) {

        if (mActivity != null && mActivity instanceof SetupVaultActivity) {
            mBinding.toolbar.setVisibility(View.GONE);
        }

        mBinding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().finish();
        });
        walletListAdapter = new WalletListAdapter(mActivity, this::handleItemClick);
        mBinding.walletList.setAdapter(walletListAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        String[] walletNames = getResources().getStringArray(getWallet());
        String[] walletValue = getResources().getStringArray(getValues());
        String[] walletSummaries = getResources().getStringArray(getWalletSummary());
        walletViewModel.setWalletInfos(walletNames, walletValue, walletSummaries);
        walletLiveData = walletViewModel.getObservableWallets();
        subscribeUi(walletLiveData);
    }

    private void subscribeUi(LiveData<List<WalletItem>> observableWallets) {
        observableWallets.observe(this, walletItems ->
                walletListAdapter.setItems(walletItems)
        );
    }

    @Override
    public void onDestroyView() {
        if (walletLiveData != null) {
            walletLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    private void handleItemClick(WalletItem walletItem) {
        LiveData<SyncMode> stepMode = walletViewModel.determineSyncMode(walletItem);
        stepMode.observe(WalletListFragment.this, mode -> {
            switch (mode) {
                case INVALID: //no address，give error message
                    break;
                case DIRECT:
                    Bundle bundleData = new Bundle();
                    bundleData.putString(Constants.KEY_WALLET_ID, walletItem.getWalletId());
                    navigate(R.id.action_to_syncFragment, bundleData);
                    break;
                case SELECT_ADDRESS:
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.KEY_WALLET_ID, walletItem.getWalletId());
                    navigate(R.id.action_to_selectAddressFragment, bundle);
                    break;
                case MULTI_CHAINS: //multi chains wallet, step int select coin page
                    Toast.makeText(mActivity, "多链钱包", Toast.LENGTH_SHORT).show();
                    break;
            }
            stepMode.removeObservers(WalletListFragment.this);
        });
    }

    private int getWallet() {
        return R.array.watch_wallet_list_remove_wallet_mode;
    }

    private int getValues() {
        return R.array.watch_wallet_ids;
    }

    private int getWalletSummary() {
        return R.array.watch_wallet_summury_remove_wallet_mode;
    }
}