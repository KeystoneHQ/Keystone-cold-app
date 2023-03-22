package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSelectNetworksBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.adapter.CheckableAdapter;
import com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.config.WalletConfig;
import com.keystone.cold.remove_wallet_mode.ui.model.CheckableItem;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectNetworksFragment extends BaseFragment<FragmentSelectNetworksBinding> {

    private CheckableAdapter checkableAdapter;

    @Override
    protected int setView() {
        return R.layout.fragment_select_networks;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);
        WalletConfig config = WalletConfig.getConfigByWalletId(walletId);

        if (checkableAdapter == null) {
            checkableAdapter = new CheckableAdapter(mActivity, new CheckableAdapter.DataStatusCallback() {
                @Override
                public void onHasValue() {
                    mBinding.ivConfirm.setEnabled(true);
                }

                @Override
                public void onEmpty() {
                    mBinding.ivConfirm.setEnabled(false);
                }
            }, config.isNetworkCheckable());
            getNetworkList(walletId).observe(this, checkableAdapter::setItems);
        }

        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.networks.setAdapter(checkableAdapter);
        mBinding.ivConfirm.setOnClickListener((v) -> {
            List<CheckableItem> items = checkableAdapter.getCheckedList();
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
            bundle.putSerializable(BundleKeys.OPENED_COINS_KEY, (Serializable) items.stream().map(CheckableItem::getId).collect(Collectors.toList()));
            navigate(R.id.action_selectNetworksFragment_to_syncFragment, bundle);
        });
    }

    private MutableLiveData<List<CheckableItem>> getNetworkList(String walletId) {
        MutableLiveData<List<CheckableItem>> listMutableLiveData = new MutableLiveData<>();
        Wallet wallet = Wallet.getWalletById(walletId);
        List<CheckableItem> list;
        switch (wallet) {
            case KEYSTONE:
                list = Arrays.asList(
                        new CheckableItem(Coins.BTC.coinId(), "Bitcoin", R.drawable.ic_coin_btc, "BTC", true),
                        new CheckableItem(Coins.ETH.coinId(), Coins.ETH.coinName(), R.drawable.ic_coin_eth, "ETH and ERC-20 tokens", true),
                        new CheckableItem("binance_smart_chain", "Binance Smart Chain", R.drawable.ic_coin_bnb, "BNB and BEP-20 tokens", true),
                        new CheckableItem(Coins.BCH.coinId(), Coins.BCH.coinName(), R.drawable.ic_coin_bch, "BCH"),
                        new CheckableItem(Coins.DASH.coinId(), Coins.DASH.coinName(), R.drawable.ic_coin_dash, "DASH"),
                        new CheckableItem(Coins.DOT.coinId(), Coins.DOT.coinName(), R.drawable.ic_coin_dot, "DOT"),
                        new CheckableItem(Coins.LTC.coinId(), Coins.LTC.coinName(), R.drawable.ic_coin_ltc, "LTC"),
                        new CheckableItem(Coins.TRON.coinId(), Coins.TRON.coinName(), R.drawable.ic_coin_trx, "TRX and TRC-20 tokens"),
                        new CheckableItem(Coins.XRP.coinId(), "XRP", R.drawable.ic_coin_xrp, "XRP")
                );
                break;
            case BITKEEP:
                list = Arrays.asList(
                        new CheckableItem(Coins.BTC.coinId(), "Bitcoin", R.drawable.ic_coin_btc, "BTC", true),
                        new CheckableItem(Coins.ETH.coinId(), Coins.ETH.coinName(), R.drawable.ic_coin_eth, " BNB,MATIC,AVAX,EVM compatible chains", true)
                );
                break;
            default:
                list = new ArrayList<>();
                break;
        }
        listMutableLiveData.postValue(list);
        return listMutableLiveData;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
