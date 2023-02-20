package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectNetworksBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.adapter.CheckableAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.CheckableItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeystoneViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectNetworksFragment extends BaseFragment<FragmentSelectNetworksBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_choose_network;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);

        CheckableAdapter checkableAdapter = new CheckableAdapter(mActivity, null);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.networks.setAdapter(checkableAdapter);
        mBinding.ivConfirm.setOnClickListener((v) -> {
            List<CheckableItem> items = checkableAdapter.getCheckedList();
            KeystoneViewModel viewModel = ViewModelProviders.of(this).get(KeystoneViewModel.class);
            viewModel.setOpenedCoins(items.stream().map(CheckableItem::getId).collect(Collectors.toList()));
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
            navigate(R.id.action_selectNetworksFragment_to_selectOneAddressFragment, bundle);
        });
        checkableAdapter.setItems(getNetworkList(walletId));
    }

    private List<CheckableItem> getNetworkList(String walletId) {
        Wallet wallet = Wallet.getWalletById(walletId);
        List<CheckableItem> list;
        if (wallet == Wallet.KEYSTONE) {
            list = Arrays.asList(
                    new CheckableItem(Coins.BTC.coinId(), "Bitcoin", R.drawable.coin_btc, "BTC", true),
                    new CheckableItem(Coins.ETH.coinId(), Coins.ETH.coinName(), R.drawable.coin_eth, "ETH and ERC-20 tokens", true),
                    new CheckableItem("binance_smart_chain", "Binance Smart Chain", R.drawable.ic_coin_bnb, "BNB and BEP-20 tokens", true),
                    new CheckableItem(Coins.BCH.coinId(), Coins.BCH.coinName(), R.drawable.coin_bch, "BCH"),
                    new CheckableItem(Coins.DASH.coinId(), Coins.DASH.coinName(), R.drawable.coin_dash, "DASH"),
                    new CheckableItem(Coins.DOT.coinId(), Coins.DOT.coinName(), R.drawable.coin_dot, "DOT"),
                    new CheckableItem(Coins.LTC.coinId(), Coins.LTC.coinName(), R.drawable.coin_ltc, "LTC"),
                    new CheckableItem(Coins.TRON.coinId(), Coins.TRON.coinName(), R.drawable.coin_tron, "TRX and TRC-20 tokens"),
                    new CheckableItem(Coins.XRP.coinId(), Coins.XRP.coinName(), R.drawable.coin_xrp, "XRP")
            );
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
