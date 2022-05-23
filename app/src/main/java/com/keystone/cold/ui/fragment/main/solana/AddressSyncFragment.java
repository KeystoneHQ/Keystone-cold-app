package com.keystone.cold.ui.fragment.main.solana;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;

import android.os.Bundle;
import android.view.View;

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

public class AddressSyncFragment extends BaseFragment<AddressSyncFragmentBinding> {


    public static final String DERIVATION_PATH_KEY = "derivation_paths_key";

    private CoinViewModel viewModel;
    private AddressSyncAdapter addressSyncAdapter;

    private final AddressSyncCallback addressSyncCallback = (addr, position) -> {
        addressSyncAdapter.toggleChecked(position);
        mBinding.tvConfirm.setEnabled(addressSyncAdapter.exitSelectedAddress());
    };


    @Override
    protected int setView() {
        return R.layout.address_sync_fragment;
    }

    @Override
    protected void init(View view) {
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
            Bundle bundle = new Bundle();
            bundle.putString(DERIVATION_PATH_KEY, addressSyncAdapter.getDerivationInfo());
            navigate(R.id.action_to_syncFragment, bundle);
        });
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

}
