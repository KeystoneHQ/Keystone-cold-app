package com.keystone.cold.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.Chains;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AccountItemBinding;
import com.keystone.cold.databinding.SelectWalletFragmentBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.keystone.cold.viewmodel.SyncViewModel;

import java.util.ArrayList;
import java.util.List;

public class SelectWalletFragment extends BaseFragment<SelectWalletFragmentBinding> {
    private AccountAdapter ledgerLiveAdapter;
    private AccountAdapter myCryptoAdapter;
    private AccountAdapter metamaskAdapter;
    protected SyncViewModel syncViewModel;
    @Override
    protected int setView() {
        return R.layout.select_wallet_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.close.setOnClickListener(v -> navigateUp());
        syncViewModel = ViewModelProviders.of(mActivity).get(SyncViewModel.class);;
        mBinding.btShowLedgerLive.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(Chains.LEDGER_LIVE);
            navigateUp();
        });
        mBinding.btShowCrypto.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(Chains.LEGACY);
            navigateUp();
        });
        mBinding.btShowMetamask.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(Chains.BIP44_STANDARD);
            navigateUp();
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        ledgerLiveAdapter = new AccountAdapter(mActivity);
        myCryptoAdapter = new AccountAdapter(mActivity);
        metamaskAdapter = new AccountAdapter(mActivity);
        syncViewModel.getAccounts(Chains.LEDGER_LIVE).observe(this, pairs -> {
            ledgerLiveAdapter.setItems(pairs);
            mBinding.rlLedgerLive.setAdapter(ledgerLiveAdapter);
        });
        syncViewModel.getAccounts(Chains.LEGACY).observe(this, pairs -> {
            myCryptoAdapter.setItems(pairs);
            mBinding.rlCrypto.setAdapter(myCryptoAdapter);
        });
        syncViewModel.getAccounts(Chains.BIP44_STANDARD).observe(this, pairs -> {
            metamaskAdapter.setItems(pairs);
            mBinding.rlMetamask.setAdapter(metamaskAdapter);
        });
    }

    static class AccountAdapter extends BaseBindingAdapter<Pair<String, String>, AccountItemBinding> {
        private List<Pair<String, String>> pairs = new ArrayList<>();
        AccountAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.account_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AccountItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding == null) return;
            binding.key.setText(items.get(position).first);
            binding.value.setText(items.get(position).second);
        }

        @Override
        protected void onBindItem(AccountItemBinding binding, Pair<String, String> item) {
        }
    }
}
