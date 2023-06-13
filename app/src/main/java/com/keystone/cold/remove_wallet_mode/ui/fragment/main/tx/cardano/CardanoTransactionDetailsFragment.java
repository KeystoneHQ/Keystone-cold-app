package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentCardanoDetailBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class CardanoTransactionDetailsFragment extends BaseFragment<FragmentCardanoDetailBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_cardano_detail;
    }

    private final MutableLiveData<CardanoTransaction> transaction;

    public CardanoTransactionDetailsFragment(MutableLiveData<CardanoTransaction> transaction) {
        this.transaction = transaction;
    }

    public static CardanoTransactionDetailsFragment newInstance(Bundle bundle, MutableLiveData<CardanoTransaction> transaction) {
        CardanoTransactionDetailsFragment fragment = new CardanoTransactionDetailsFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void init(View view) {
        transaction.observe(this, v -> {
            if (v == null) return;
            mBinding.setTx(v.getDetail());
            mBinding.setCoinCode(Coins.ADA.coinCode());
            mBinding.setCheckInfoTitle(Coins.ADA.coinName());
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
