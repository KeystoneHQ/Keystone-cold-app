package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AddressSyncFragmentBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.AddressSyncAdapter;
import com.keystone.cold.ui.fragment.main.AddressSyncCallback;
import com.keystone.cold.viewmodel.CoinViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class AddressSyncFragment extends BaseFragment<AddressSyncFragmentBinding> implements Toolbar.OnMenuItemClickListener {


    private CoinViewModel viewModel;
    private AddressSyncAdapter addressSyncAdapter;

    private final AddressSyncCallback addressSyncCallback = (addr, position) -> {
        addressSyncAdapter.toggleChecked(position);
        Bundle bundle = new Bundle();
        bundle.putString("sync_addresses", addressSyncAdapter.getDerivationPaths());
        navigate(R.id.action_to_syncFragment, bundle);
    };


    @Override
    protected int setView() {
        return R.layout.address_sync_fragment;
    }

    @Override
    protected void init(View view) {
        addressSyncAdapter = new AddressSyncAdapter(mActivity);
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
        //mBinding.toolbar.inflateMenu(R.menu.confirm);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = requireArguments();
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(),
                data.getString(KEY_COIN_ID));
        viewModel = ViewModelProviders.of(getParentFragment(), factory)
                .get(CoinViewModel.class);
        subscribeUi(viewModel.getAddress());
    }

    private void subscribeUi(LiveData<List<AddressEntity>> address) {
        String code = Utilities.getCurrentSolAccount(mActivity);
        SOLAccount account = SOLAccount.ofCode(code);

        address.observe(this, addressEntities -> {
            addressEntities = addressEntities.stream()
                    .filter(addressEntity ->
                            account.isChildrenPath(addressEntity.getPath()))
                    .peek(addressEntity -> {
                        addressEntity.setDisplayName(addressEntity.getName());
                    }).collect(Collectors.toList());
            addressSyncAdapter.setItems(addressEntities);
        });
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_confirm:
                Bundle bundle = new Bundle();
                bundle.putString("sync_addresses", addressSyncAdapter.getDerivationPaths());
                navigate(R.id.action_to_syncFragment, bundle);
                break;
            default:
                break;
        }
        return true;
    }
}
