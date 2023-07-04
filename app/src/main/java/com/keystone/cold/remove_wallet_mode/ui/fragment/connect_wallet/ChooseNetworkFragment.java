package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentChooseNetworkBinding;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.remove_wallet_mode.ui.adapter.ClickableAdapter;
import com.keystone.cold.remove_wallet_mode.ui.model.ClickableItem;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChooseNetworkFragment extends BaseFragment<FragmentChooseNetworkBinding> {
    @Override
    protected int setView() {
        return R.layout.fragment_choose_network;
    }

    DataRepository mRepository;

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);

        mRepository = MainApplication.getApplication().getRepository();

        ClickableAdapter.ClickableCallback callback = (item) -> {
            LiveData<Destination> destinationLiveData = getNextDestination(item);
            destinationLiveData.observe(this, (v) -> {
                navigate(v.id, v.bundle);
            });
        };
        ClickableAdapter clickableAdapter = new ClickableAdapter(mActivity, callback);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.networks.setAdapter(clickableAdapter);
        clickableAdapter.setItems(getNetworkList(walletId));
    }

    private LiveData<Destination> getNextDestination(ClickableItem item) {
        MutableLiveData<Destination> destinationMutableLiveData = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            Bundle data = requireArguments();
            String walletId = data.getString(BundleKeys.WALLET_ID_KEY);
            Boolean canSelectMultiAddress = item.getId().equals(Coins.SUI.coinId()) || item.getId().equals(Coins.APTOS.coinId());
            if (item.getId().equals(Coins.DOT.coinId()) || item.getId().equals(Coins.KSM.coinId()) || canSelectMultiAddress) {
                List<AddressEntity> addressEntities = MainApplication.getApplication().getRepository().loadAddressSync(item.getId());
                if (addressEntities.size() == 1) {
                    AddressEntity entity = addressEntities.get(0);
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
                    bundle.putSerializable(BundleKeys.ADDRESS_IDS_KEY, (Serializable) Collections.singletonList(entity.getId()));
                    bundle.putString(BundleKeys.COIN_ID_KEY, entity.getCoinId());
                    destinationMutableLiveData.postValue(new Destination(R.id.action_to_syncFragment, bundle));
                } else if (canSelectMultiAddress) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
                    bundle.putString(BundleKeys.COIN_ID_KEY, item.getId());
                    destinationMutableLiveData.postValue(new Destination(R.id.action_to_selectAddressFragment, bundle));
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
                    bundle.putString(BundleKeys.COIN_ID_KEY, item.getId());
                    destinationMutableLiveData.postValue(new Destination(R.id.action_chooseNetworkFragment_to_selectOneAddressFragment, bundle));
                }
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.WALLET_ID_KEY, walletId);
                bundle.putString(BundleKeys.COIN_ID_KEY, item.getId());
                destinationMutableLiveData.postValue(new Destination(R.id.action_chooseNetworkFragment_to_selectOneAddressFragment, bundle));
            }
        });
        return destinationMutableLiveData;
    }

    private List<ClickableItem> getNetworkList(String walletId) {
        Wallet wallet = Wallet.getWalletById(walletId);
        List<ClickableItem> list;
        if (wallet == Wallet.POLKADOTJS || wallet == Wallet.SUBWALLET) {
            list = Arrays.asList(
                    new ClickableItem(Coins.DOT.coinId(), Coins.DOT.coinName(), R.drawable.coin_dot),
                    new ClickableItem(Coins.KSM.coinId(), Coins.KSM.coinName(), R.drawable.coin_ksm)
            );
        } else if (wallet == Wallet.FEWCHA) {
            list = Arrays.asList(
                    new ClickableItem(Coins.SUI.coinId(), Coins.SUI.coinName(), R.drawable.coin_sui),
                    new ClickableItem(Coins.APTOS.coinId(), Coins.APTOS.coinName(), R.drawable.coin_apt)
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
