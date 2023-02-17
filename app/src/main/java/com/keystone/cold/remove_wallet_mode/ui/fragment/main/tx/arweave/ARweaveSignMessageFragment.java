package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.arweave;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.SignMessageFragment;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.ARweaveTxViewModel;


public class ARweaveSignMessageFragment extends SignMessageFragment<ARweaveTxViewModel> {

    @Override
    protected String getCoinName() {
        return Coins.AR.coinName();
    }

    @Override
    protected String getCoinCode() {
        return Coins.AR.coinCode();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ARweaveTxViewModel.class);
    }

}
