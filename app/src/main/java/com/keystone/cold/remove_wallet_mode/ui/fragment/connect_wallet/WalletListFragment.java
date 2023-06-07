package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_AUTH_RESULT_KEY;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_REJECTED;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_STATUS_KEY;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.ArweaveAuthFragment.AR_SETUP_SUCCESS;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentWalletListBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.helper.SyncMode;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.remove_wallet_mode.ui.adapter.WalletListAdapter;
import com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.config.WalletConfig;
import com.keystone.cold.remove_wallet_mode.ui.model.WalletItem;
import com.keystone.cold.remove_wallet_mode.viewmodel.WalletViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;

import java.util.List;

public class WalletListFragment extends BaseFragment<FragmentWalletListBinding> {

    private LiveData<List<WalletItem>> walletLiveData;

    private WalletListAdapter walletListAdapter;
    private WalletViewModel walletViewModel;

    @Override
    protected int setView() {
        return R.layout.fragment_wallet_list;
    }

    @Override
    protected void init(View view) {

        if (mActivity != null && mActivity instanceof SetupVaultActivity) {
            mBinding.toolbar.setVisibility(View.GONE);
        }

        mBinding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().finish();
        });
        walletListAdapter = new WalletListAdapter(mActivity, this::handleItemClick);
        mBinding.walletList.setAdapter(walletListAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        String[] walletNames = getResources().getStringArray(getWallet());
        String[] walletValue = getResources().getStringArray(getValues());
        String[] walletSummaries = getResources().getStringArray(getWalletSummary());
        walletViewModel.setWalletInfos(walletNames, walletValue, walletSummaries);
        walletLiveData = walletViewModel.getObservableWallets();
        subscribeUi(walletLiveData);
    }

    private void subscribeUi(LiveData<List<WalletItem>> observableWallets) {
        observableWallets.observe(this, walletItems ->
                walletListAdapter.setItems(walletItems)
        );
    }

    @Override
    public void onDestroyView() {
        if (walletLiveData != null) {
            walletLiveData.removeObservers(this);
        }
        super.onDestroyView();
    }

    private void handleItemClick(WalletItem walletItem) {
        if (walletItem.getWalletId().equals(Wallet.ARCONNECT.getWalletId())) {
            handleArweaveProcess(walletItem);
        } else {
            handleNormalProcess(walletItem);
        }
    }

    private void handleNormalProcess(WalletItem walletItem) {
        LiveData<SyncMode> stepMode = walletViewModel.determineSyncMode(walletItem.getWalletId());
        stepMode.observe(WalletListFragment.this, mode -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, walletItem.getWalletId());
            WalletConfig config = WalletConfig.getConfigByWalletId(walletItem.getWalletId());
            bundle.putString(BundleKeys.COIN_ID_KEY, config.getCoinId());
            switch (mode) {
                case INVALID: //no address，give error message
                    break;
                case DIRECT:
                    // export xpub to sync
                    navigate(R.id.action_to_syncFragment, bundle);
                    break;
                case SUBSTRATE:
                    // select network, select one account, then sync.
                    navigate(R.id.action_to_chooseNetworkFragment, bundle);
                    break;
                case SELECT_ADDRESS:
                    // select accounts to sync
                    navigate(R.id.action_to_selectAddressFragment, bundle);
                    break;
                case SELECT_ONE_ADDRESS:
                    // select one account to sync
                    navigate(R.id.action_to_selectOneAddressFragment, bundle);
                    break;
                case KEY_REQUEST:
                    navigate(R.id.action_walletListFragment_to_keyRequestFragment, bundle);
                    break;
                case SELECT_COINS:
                    navigate(R.id.action_walletListFragment_to_selectNetworksFragment, bundle);
                    break;
            }
            stepMode.removeObservers(WalletListFragment.this);
        });
    }

    private void handleArweaveProcess(WalletItem walletItem) {
        ArweaveViewModel arweaveViewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
        LiveData<Boolean> hasAR = arweaveViewModel.hasArweaveAddress();
        hasAR.observe(this, (v) -> {
            if (!v) {
                ModalDialog.showRemindModal(mActivity, getString(R.string.arweave_authenticate_hint), getString(R.string.add), () -> {
                    navigate(R.id.action_to_ArweaveAuthFragment);
                    FragmentManager fragmentManager = this.getParentFragmentManager();
                    fragmentManager.setFragmentResultListener(AR_AUTH_RESULT_KEY, this, (s, bundle) -> {
                        String result = bundle.getString(AR_SETUP_STATUS_KEY);
                        switch (result) {
                            case AR_SETUP_SUCCESS:
                                handleNormalProcess(walletItem);
                            case AR_SETUP_REJECTED:
                                fragmentManager.clearFragmentResultListener(AR_AUTH_RESULT_KEY);
                                break;
                        }
                    });
                });
            } else {
                handleNormalProcess(walletItem);
            }
            hasAR.removeObservers(this);
        });

    }

    private int getWallet() {
        return R.array.watch_wallet_list_remove_wallet_mode;
    }

    private int getValues() {
        return R.array.watch_wallet_ids;
    }

    private int getWalletSummary() {
        return R.array.watch_wallet_summury_remove_wallet_mode;
    }
}