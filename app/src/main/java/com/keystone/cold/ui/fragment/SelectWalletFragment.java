package com.keystone.cold.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.R;
import com.keystone.cold.databinding.AccountItemBinding;
import com.keystone.cold.databinding.SelectWalletFragmentBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.viewmodel.SyncViewModel;

import java.util.ArrayList;
import java.util.List;

public class SelectWalletFragment extends BaseFragment<SelectWalletFragmentBinding> {
    private AccountAdapter ledgerLiveAdapter;
    private AccountAdapter myCryptoAdapter;
    private AccountAdapter metamaskAdapter;
    protected SyncViewModel syncViewModel;
    private final static String DERIVATION_PATH = "Derivation Path: ";

    @Override
    protected int setView() {
        return R.layout.select_wallet_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.close.setOnClickListener(v -> navigateUp());
        syncViewModel = ViewModelProviders.of(mActivity).get(SyncViewModel.class);
        mBinding.btShowLedgerLive.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.LEDGER_LIVE);
            navigateUp();
        });
        mBinding.btShowCrypto.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.LEDGER_LEGACY);
            navigateUp();
        });
        mBinding.btShowMetamask.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.BIP44_STANDARD);
            navigateUp();
        });
        mBinding.derivationLive.setText(DERIVATION_PATH + ETHAccount.LEDGER_LIVE.getDisplayPath());
        mBinding.derivationLegacy.setText(DERIVATION_PATH + ETHAccount.LEDGER_LEGACY.getDisplayPath());
        // both ledger legacy and bip44 standard use M/44'/60'/0
        mBinding.derivationBip44.setText(DERIVATION_PATH + ETHAccount.BIP44_STANDARD.getDisplayPath());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        ledgerLiveAdapter = new AccountAdapter(mActivity);
        myCryptoAdapter = new AccountAdapter(mActivity);
        metamaskAdapter = new AccountAdapter(mActivity);
        syncViewModel.getAccounts(ETHAccount.LEDGER_LEGACY).observe(this, pairs -> {
            myCryptoAdapter.setItems(pairs);
            mBinding.rlCrypto.setAdapter(myCryptoAdapter);
        });
        syncViewModel.getAccounts(ETHAccount.BIP44_STANDARD).observe(this, pairs -> {
            metamaskAdapter.setItems(pairs);
            mBinding.rlMetamask.setAdapter(metamaskAdapter);
        });
        syncViewModel.getAccounts(ETHAccount.LEDGER_LIVE).observe(this, pairs -> {
            ledgerLiveAdapter.setItems(pairs);
            mBinding.rlLedgerLive.setAdapter(ledgerLiveAdapter);
        });
    }

    static class AccountAdapter extends BaseBindingAdapter<Pair<String, String>, AccountItemBinding> {
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
