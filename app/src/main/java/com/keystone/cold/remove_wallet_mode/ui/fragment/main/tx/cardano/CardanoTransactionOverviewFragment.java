package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentCardanoOverviewBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class CardanoTransactionOverviewFragment extends BaseFragment<FragmentCardanoOverviewBinding> {

    private final MutableLiveData<CardanoTransaction> transaction;

    @Override
    protected int setView() {
        return R.layout.fragment_cardano_overview;
    }

    public CardanoTransactionOverviewFragment(MutableLiveData<CardanoTransaction> transaction) {
        this.transaction = transaction;
    }

    public static CardanoTransactionOverviewFragment newInstance(Bundle bundle, MutableLiveData<CardanoTransaction> transaction) {
        CardanoTransactionOverviewFragment fragment = new CardanoTransactionOverviewFragment(transaction);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void init(View view) {
        transaction.observe(this, v -> {
            if (v == null) return;
            mBinding.setTx(v.getOverview());
            mBinding.setCoinCode(Coins.ADA.coinCode());
            mBinding.setCheckInfoTitle(Coins.ADA.coinName());
            if (v.getSigned() != null) {
                mBinding.qr.setVisibility(View.VISIBLE);
                mBinding.qrcode.qrcode.setData(v.getSigned());
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
