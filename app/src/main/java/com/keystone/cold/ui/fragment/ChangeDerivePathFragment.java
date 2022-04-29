package com.keystone.cold.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.AccountSolItemBinding;
import com.keystone.cold.databinding.ChangeDerivationPathFragmentBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.viewmodel.SyncViewModel;
import com.keystone.cold.viewmodel.WatchWallet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeDerivePathFragment extends BaseFragment<ChangeDerivationPathFragmentBinding> {

    private WatchWallet watchWallet;
    protected SyncViewModel syncViewModel;

    private AccountAdapter accountAdapter1;
    private AccountAdapter accountAdapter2;
    private AccountAdapter accountAdapter3;

    private String selectCode;

    private static final String[] SOL_ACCOUNT_CODES = {SOLAccount.SOLFLARE_BIP44.getCode(), SOLAccount.SOLFLARE_BIP44_ROOT.getCode(), SOLAccount.SOLFLARE_BIP44_CHANGE.getCode()};
    private static final String[] ETH_ACCOUNT_CODES = {ETHAccount.LEDGER_LIVE.getCode(), ETHAccount.LEDGER_LEGACY.getCode(), ETHAccount.BIP44_STANDARD.getCode()};

    @Override
    protected int setView() {
        return R.layout.change_derivation_path_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.btConfirm.setOnClickListener(v -> stepIntoMainActivity());
        syncViewModel = ViewModelProviders.of(mActivity).get(SyncViewModel.class);
        if (watchWallet == WatchWallet.METAMASK) {
            setupMetaMaskUI();
        } else if (watchWallet == WatchWallet.SOLANA) {
            setupSolUI();
        }
    }

    private void setupSolUI() {
        String code = Utilities.getCurrentSolAccount(mActivity);
        setCardCheckedStatus(code, SOL_ACCOUNT_CODES);

        mBinding.patternCard1.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44.getCode(), SOL_ACCOUNT_CODES);
        });
        mBinding.patternCard2.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44_ROOT);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44_ROOT.getCode(), SOL_ACCOUNT_CODES);
        });
        mBinding.patternCard3.setOnClickListener(v -> {
            syncViewModel.getSolAccountMutableLiveData().postValue(SOLAccount.SOLFLARE_BIP44_CHANGE);
            setCardCheckedStatus(SOLAccount.SOLFLARE_BIP44_CHANGE.getCode(), SOL_ACCOUNT_CODES);
        });
        mBinding.derivationPattern1.setText(SOLAccount.SOLFLARE_BIP44.getDisplayPath());
        mBinding.derivationPattern2.setText(SOLAccount.SOLFLARE_BIP44_ROOT.getDisplayPath());
        mBinding.derivationPattern3.setText(SOLAccount.SOLFLARE_BIP44_CHANGE.getDisplayPath());

    }

    private void setupMetaMaskUI() {
        String code = Utilities.getCurrentEthAccount(mActivity);
        setCardCheckedStatus(code, ETH_ACCOUNT_CODES);

        mBinding.patternCard1.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.LEDGER_LIVE);
            setCardCheckedStatus(ETHAccount.LEDGER_LIVE.getCode(), ETH_ACCOUNT_CODES);
        });
        mBinding.patternCard2.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.LEDGER_LEGACY);
            setCardCheckedStatus(ETHAccount.LEDGER_LEGACY.getCode(), ETH_ACCOUNT_CODES);
        });
        mBinding.patternCard3.setOnClickListener(v -> {
            syncViewModel.getChainsMutableLiveData().postValue(ETHAccount.BIP44_STANDARD);
            setCardCheckedStatus(ETHAccount.BIP44_STANDARD.getCode(), ETH_ACCOUNT_CODES);
        });
        mBinding.derivationPattern1.setText(highLight(ETHAccount.LEDGER_LIVE.getDisplayPath() + " (" + ETHAccount.LEDGER_LIVE.getName() + ")"));
        mBinding.derivationPattern2.setText(highLight(ETHAccount.LEDGER_LEGACY.getDisplayPath() + " (" + ETHAccount.LEDGER_LEGACY.getName() + ")"));
        mBinding.derivationPattern3.setText(highLight(ETHAccount.BIP44_STANDARD.getDisplayPath() + " (" + ETHAccount.BIP44_STANDARD.getName() + ")"));

    }

    private void setCardCheckedStatus(final String code, final String[] codes) {
        selectCode = code;
        int checkedIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (code.equals(codes[i])) {
                checkedIndex = i;
                break;
            }
        }
        mBinding.patternCard1.setBackgroundResource(checkedIndex == 0 ? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
        mBinding.patternCard2.setBackgroundResource(checkedIndex == 1 ? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
        mBinding.patternCard3.setBackgroundResource(checkedIndex == 2 ? R.drawable.bg_change_path_card : R.drawable.bg_change_path_card_unchecked);
    }

    private void stepIntoMainActivity() {
        if (watchWallet == WatchWallet.METAMASK) {
            if (!TextUtils.isEmpty(selectCode)) {
                Utilities.setCurrentEthAccount(mActivity, selectCode);
            }
        } else if (watchWallet == WatchWallet.SOLANA) {
            if (!TextUtils.isEmpty(selectCode)) {
                Utilities.setCurrentSolAccount(mActivity, selectCode);
            }
        }
        startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initData(Bundle savedInstanceState) {
        accountAdapter1 = new AccountAdapter(mActivity);
        accountAdapter2 = new AccountAdapter(mActivity);
        accountAdapter3 = new AccountAdapter(mActivity);

        mBinding.addressList1.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mBinding.patternCard1.performClick();
            }
            return false;
        });
        mBinding.addressList2.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mBinding.patternCard2.performClick();
            }
            return false;
        });
        mBinding.addressList3.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mBinding.patternCard3.performClick();
            }
            return false;
        });

        if (watchWallet == WatchWallet.METAMASK) {
            setMetaMaskData();
        } else if (watchWallet == WatchWallet.SOLANA) {
            setSolData();
        }
    }

    private void setSolData() {

        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44).observe(this, pairs -> {
            accountAdapter1.setItems(pairs);
            mBinding.addressList1.setAdapter(accountAdapter1);
        });
        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44_ROOT).observe(this, pairs -> {
            accountAdapter2.setItems(pairs);
            mBinding.addressList2.setAdapter(accountAdapter2);
        });
        syncViewModel.getSolAccounts(SOLAccount.SOLFLARE_BIP44_CHANGE).observe(this, pairs -> {
            accountAdapter3.setItems(pairs);
            mBinding.addressList3.setAdapter(accountAdapter3);
        });
    }

    private void setMetaMaskData() {

        syncViewModel.getAccounts(ETHAccount.LEDGER_LIVE).observe(this, pairs -> {
            accountAdapter1.setItems(pairs);
            mBinding.addressList1.setAdapter(accountAdapter1);
        });
        syncViewModel.getAccounts(ETHAccount.LEDGER_LEGACY).observe(this, pairs -> {
            accountAdapter2.setItems(pairs);
            mBinding.addressList2.setAdapter(accountAdapter2);
        });
        syncViewModel.getAccounts(ETHAccount.BIP44_STANDARD).observe(this, pairs -> {
            accountAdapter3.setItems(pairs);
            mBinding.addressList3.setAdapter(accountAdapter3);
        });
    }

    public static Pattern pattern = Pattern.compile("\\(.+\\)");

    public static SpannableStringBuilder highLight(String content) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(content);
        Matcher matcher = pattern.matcher(spannable);
        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(R.color.icon_select)), matcher.start() + 1,
                    matcher.end() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spannable;
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
