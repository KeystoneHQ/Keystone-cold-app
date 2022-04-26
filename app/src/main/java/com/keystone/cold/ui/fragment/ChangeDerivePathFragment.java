package com.keystone.cold.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AccountSolItemBinding;
import com.keystone.cold.databinding.ChangeDerivationPathFragmentBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.viewmodel.SyncViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

public class ChangeDerivePathFragment extends BaseFragment <ChangeDerivationPathFragmentBinding>{

    private WatchWallet watchWallet;
    protected SyncViewModel syncViewModel;

    private AccountAdapter accountAdapter1;
    private AccountAdapter accountAdapter2;
    private AccountAdapter accountAdapter3;

    private String selectCode;

    @Override
    protected int setView() {
        return R.layout.change_derivation_path_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.btConfirm.setOnClickListener(v-> stepIntoMainActivity());
        //fillUI();

        syncViewModel = ViewModelProviders.of(mActivity).get(SyncViewModel.class);
        String code = Utilities.getCurrentSolAccount(mActivity);
        setCardCheckedStatus(code);

        mBinding.bip44Card.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44.getCode());
        });
        mBinding.bip44RootCard.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44_ROOT);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44_ROOT.getCode());
        });
        mBinding.bip44ChangeCard.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44_CHANGE);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44_CHANGE.getCode());
        });
        mBinding.derivationBip44.setText(SOLAccount.SOLFLARE_BIP44.getDisplayPath());
        mBinding.derivationBip44Root.setText(SOLAccount.SOLFLARE_BIP44_ROOT.getDisplayPath());
        mBinding.derivationBip44Change.setText(SOLAccount.SOLFLARE_BIP44_CHANGE.getDisplayPath());

    }

    private void setCardCheckedStatus(String code) {
        selectCode = code;
        int checkedIndex = 1;
        if (code.equals(SOLAccount.SOLFLARE_BIP44.getCode())) {
            checkedIndex = 1;
        } else if (code.equals(SOLAccount.SOLFLARE_BIP44_ROOT.getCode())) {
            checkedIndex = 2;
        } else if (code.equals(SOLAccount.SOLFLARE_BIP44_CHANGE.getCode())) {
            checkedIndex = 3;
        }
        mBinding.bip44Card.setBackgroundResource(checkedIndex == 1? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
        mBinding.bip44RootCard.setBackgroundResource(checkedIndex == 2? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
        mBinding.bip44ChangeCard.setBackgroundResource(checkedIndex == 3? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
    }

    private void stepIntoMainActivity(){
        if (!TextUtils.isEmpty(selectCode)){
            Utilities.setCurrentSolAccount(mActivity, selectCode);
        }
        startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();
    }

    //后期考虑改为动态添加
    private void fillUI() {
        switch (watchWallet) {
            case SOLANA:
                fillSolList();
                break;
            case METAMASK:
                break;
        }

    }

    private void fillSolList() {
//        mBinding.llListRoot.removeAllViews();
//        for (int i = 0; i < Coins.SOL.getAccounts().length; i++){
//
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.topMargin =
//            SpanedTextView spanedTextView = new SpanedTextView(mActivity);
//            spanedTextView.setTextColor(getResources().getColor(R.color.white));
//
//
//        }
//        mBinding.llListRoo
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initData(Bundle savedInstanceState) {
        accountAdapter1 = new AccountAdapter(mActivity);
        accountAdapter2 = new AccountAdapter(mActivity);
        accountAdapter3 = new AccountAdapter(mActivity);

        mBinding.bip44List.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP){
                mBinding.bip44Card.performClick();
            }
            return false;
        });
        mBinding.bip44RootList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP){
                mBinding.bip44RootCard.performClick();
            }
            return false;
        });
        mBinding.bip44ChangeList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP){
                mBinding.bip44ChangeCard.performClick();
            }
            return false;
        });

        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44).observe(this, pairs -> {
            accountAdapter1.setItems(pairs);
            mBinding.bip44List.setAdapter(accountAdapter1);
        });
        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44_ROOT).observe(this, pairs -> {
            accountAdapter2.setItems(pairs);
            mBinding.bip44RootList.setAdapter(accountAdapter2);
        });
        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44_CHANGE).observe(this, pairs -> {
            accountAdapter3.setItems(pairs);
            mBinding.bip44ChangeList.setAdapter(accountAdapter3);
        });
    }


    static class AccountAdapter extends BaseBindingAdapter<Pair<String, String>, AccountSolItemBinding> {
        AccountAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.account_sol_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AccountSolItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding == null) return;
            binding.key.setText(items.get(position).first);
            binding.value.setText(items.get(position).second);
        }

        @Override
        protected void onBindItem(AccountSolItemBinding binding, Pair<String, String> item) {
        }
    }
}
