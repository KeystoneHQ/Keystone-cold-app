package com.keystone.cold.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.SyncFragment.DERIVATION_PATH_KEY;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AddressSyncFragmentBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.CoinViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class AddressSyncFragment extends BaseFragment<AddressSyncFragmentBinding> {

    private CoinViewModel viewModel;
    private AddressSyncAdapter addressSyncAdapter;
    private WatchWallet watchWallet;

    private final AddressSyncCallback addressSyncCallback = (addr, position) -> {
        addressSyncAdapter.toggleChecked(position);
        if (watchWallet == WatchWallet.NEAR || watchWallet.equals(WatchWallet.POLKADOT_JS)) {
            stepIntoSync();
            addressSyncAdapter.resetCheckStatus();
        } else {
            mBinding.tvConfirm.setEnabled(addressSyncAdapter.existSelectedAddress());
        }
    };


    @Override
    protected int setView() {
        return R.layout.address_sync_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);

        if (addressSyncAdapter == null) {
            addressSyncAdapter = new AddressSyncAdapter(mActivity);
            mBinding.tvConfirm.setEnabled(false);
        }
        addressSyncAdapter.setAddressSyncCallback(addressSyncCallback);
        mBinding.addrList.setAdapter(addressSyncAdapter);
        addressSyncAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mBinding.empty.setVisibility(View.GONE);
                mBinding.addrList.setVisibility(View.VISIBLE);
            }
        });
        mBinding.tvConfirm.setOnClickListener(v -> {
            stepIntoSync();
        });
        if (watchWallet == WatchWallet.NEAR || watchWallet.equals(WatchWallet.POLKADOT_JS)) {
            mBinding.tvConfirm.setVisibility(View.GONE);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

    }

    private void stepIntoSync() {
        Bundle bundle = new Bundle();
        Bundle data = requireArguments();
        String coinId = data.getString(KEY_COIN_ID);
        String coinCode = Coins.coinCodeFromCoinId(coinId);
        bundle.putString("coinCode", coinCode);
        bundle.putSerializable(DERIVATION_PATH_KEY, (Serializable) addressSyncAdapter.getSyncInfo());
        navigate(R.id.action_to_syncFragment, bundle);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = requireArguments();
        String coinId = data.getString(KEY_COIN_ID);
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(),
                data.getString(KEY_COIN_ID));
        viewModel = ViewModelProviders.of(getParentFragment(), factory)
                .get(CoinViewModel.class);
        subscribeUi(viewModel.getNewAddressLiveData(coinId));
    }

    private void subscribeUi(LiveData<List<AddressEntity>> address) {
        address.observe(this, addressEntities -> {
            addressEntities = addressEntities.stream()
                    .filter(this::isBelongToCurrentAccount)
                    .peek(this::handleDisplayName)
                    .collect(Collectors.toList());
            addressSyncAdapter.setItems(addressEntities);
        });
    }

    private void handleDisplayName(AddressEntity addressEntity) {
        switch (watchWallet) {
            case NEAR:
                if (addressEntity.getName().startsWith("NEAR-")) {
                    addressEntity.setDisplayName(addressEntity.getName().replace("NEAR-", "Account "));
                } else {
                    addressEntity.setDisplayName(addressEntity.getName());
                }
                break;
            default:
                addressEntity.setDisplayName(addressEntity.getName());
                break;
        }
    }


    private boolean isBelongToCurrentAccount(AddressEntity entity) {
        switch (watchWallet) {
            case SOLANA: {
                String code = Utilities.getCurrentSolAccount(mActivity);
                SOLAccount account = SOLAccount.ofCode(code);
                return account.isChildrenPath(entity.getPath());
            }
            case NEAR: {
                String code = Utilities.getCurrentNearAccount(mActivity);
                NEARAccount account = NEARAccount.ofCode(code);
                return account.isChildrenPath(entity.getPath());
            }
            default:
                return true;
        }
    }
}
