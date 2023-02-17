package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum;


import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.EthereumTxViewModel;


public class EthereumSignMessageFragment extends SignMessageFragment<EthereumTxViewModel> {

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(EthereumTxViewModel.class);
    }

    @Override
    protected String getCoinName() {
        return Coins.ETH.coinName();
    }

    @Override
    protected String getCoinCode() {
        return Coins.ETH.coinCode();
    }
}
