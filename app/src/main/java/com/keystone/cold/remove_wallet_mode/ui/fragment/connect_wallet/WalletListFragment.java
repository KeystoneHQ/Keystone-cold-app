package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentWalletListBinding;
import com.keystone.cold.remove_wallet_mode.ui.adapter.WalletListAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;
import com.keystone.cold.remove_wallet_mode.ui.status.AddressDetectStatus;
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
        LiveData<AddressDetectStatus> stepMode = walletViewModel.detectWalletItem(walletItem);
        stepMode.observe(WalletListFragment.this, mode -> {
            switch (mode) {
                case NO_ADDRESS: //no address，give error message
                    break;
                case ONE_ADDRESS: //one address, jump sync page directly
                    Toast.makeText(mActivity, "一个地址", Toast.LENGTH_SHORT).show();
                    break;
                case MULTI_ADDRESSES: //more than one address, jump select address page
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.KEY_WALLET_ID, walletItem.getWalletId());
                    navigate(R.id.action_to_selectAddressFragment, bundle);
//                    Toast.makeText(mActivity, "多个地址", Toast.LENGTH_SHORT).show();
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