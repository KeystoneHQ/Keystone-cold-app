package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentSyncBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.BlueWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.FewchaWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.MetamaskViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SenderWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SolFlareViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SubstrateWalletViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.sparrowwallet.hummingbird.UR;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SyncFragment extends BaseFragment<FragmentSyncBinding> {
    private Wallet wallet;
    private List<Long> addressIds;

    @Override
    protected int setView() {
        return R.layout.fragment_sync;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        Bundle data = getArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);
        if (data.containsKey(BundleKeys.ADDRESS_IDS_KEY)) {
            addressIds = (List<Long>) data.getSerializable(BundleKeys.ADDRESS_IDS_KEY);
        }
        wallet = Wallet.getWalletById(walletId);
        mBinding.setWallet(wallet.getWalletName());

        mBinding.complete.setOnClickListener(v -> {
            if (mActivity != null && mActivity instanceof SetupVaultActivity) {
                startActivity(new Intent(mActivity, MainActivity.class));
            }
            mActivity.finish();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.more, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        switch (wallet) {
            case POLKADOTJS:
            case SUBWALLET:
                generateSubstrateSyncData();
                break;
            default:
                generateURSyncData();
        }
    }

    private void generateSubstrateSyncData() {
        MutableLiveData<String> substrateSyncData;
        SubstrateWalletViewModel substrateWalletViewModel = ViewModelProviders.of(this).get(SubstrateWalletViewModel.class);
        substrateWalletViewModel.setCoinId(requireArguments().getString(BundleKeys.COIN_ID_KEY));
        substrateWalletViewModel.setAddressId(addressIds.get(0));
        substrateSyncData = substrateWalletViewModel.generateSyncData();
        if (substrateSyncData != null) {
            substrateSyncData.observe(this, data -> {
                if (data != null) {
                    mBinding.dynamicQrcodeLayout.qrcode.disableMultipart();
                    mBinding.dynamicQrcodeLayout.qrcode.setData(data);
                    substrateSyncData.removeObservers(this);
                }
            });
        }
    }

    private void generateURSyncData() {
        MutableLiveData<UR> urMutableLiveData;
        switch (wallet) {
            case FEWCHA:
                FewchaWalletViewModel fewchaWalletViewModel = ViewModelProviders.of(this).get(FewchaWalletViewModel.class);
                fewchaWalletViewModel.setAddressIds(addressIds);
                urMutableLiveData = fewchaWalletViewModel.generateSyncUR();
                break;
            case METAMASK:
                MetamaskViewModel metamaskViewModel = ViewModelProviders.of(this).get(MetamaskViewModel.class);
                urMutableLiveData = metamaskViewModel.generateSyncUR();
                break;
            case BLUEWALLET:
                BlueWalletViewModel blueWalletViewModel = ViewModelProviders.of(this).get(BlueWalletViewModel.class);
                urMutableLiveData = blueWalletViewModel.generateSyncUR();
                break;
            case SOLFLARE:
                SolFlareViewModel solFlareViewModel = ViewModelProviders.of(this).get(SolFlareViewModel.class);
                solFlareViewModel.setAddressIds(addressIds);
                urMutableLiveData = solFlareViewModel.generateSyncUR();
                break;
            case SENDER:
                SenderWalletViewModel senderWalletViewModel = ViewModelProviders.of(this).get(SenderWalletViewModel.class);
                senderWalletViewModel.setAddressIds(addressIds);
                urMutableLiveData = senderWalletViewModel.generateSyncUR();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + wallet);
        }
        if (urMutableLiveData != null) {
            urMutableLiveData.observe(this, ur -> {
                if (ur != null) {
                    mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                    urMutableLiveData.removeObservers(this);
                }
            });
        }
    }


    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        WalletConfig config = WalletConfig.getConfigByWalletId(wallet.getWalletId());
        if (config.isShowSelectAddress()) {
            if (wallet.equals(Wallet.POLKADOTJS) || wallet.equals(Wallet.SUBWALLET)) {
                binding.selectAccountText.setText(R.string.select_another_account);
            }
            binding.rlSelectAddress.setVisibility(View.VISIBLE);
        }
        if (config.isShowTutorial()) {
            binding.rlTutorial.setVisibility(View.VISIBLE);
        }
        if (config.isShowChangePath()) {
            binding.rlChangePath.setVisibility(View.VISIBLE);
        }

        binding.rlSelectAddress.setOnClickListener(v -> {
            if (wallet.equals(Wallet.POLKADOTJS) || wallet.equals(Wallet.SUBWALLET)) {
                navigateUp();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
                bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY));
                navigate(R.id.action_to_selectOneAddressFragment, bundle);
            } else {
                navigateUp();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
                navigate(R.id.action_to_selectAddressFragment, bundle);
            }
            dialog.dismiss();
        });
        binding.rlTutorial.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
            navigate(R.id.action_to_tutorialsFragment, bundle);
            dialog.dismiss();
        });
        binding.rlChangePath.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.COIN_ID_KEY, config.getCoinId());
            bundle.putString(BundleKeys.WALLET_ID_KEY, config.getWalletId());
            navigate(R.id.action_to_changeDerivationPathFragment, bundle);
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private enum WalletConfig {
        METAMASK(Wallet.METAMASK.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        FEWCHA(Wallet.FEWCHA.getWalletId(), new String[]{Coins.APTOS.coinId()}, false, true, true),
        SOLFLARE(Wallet.SOLFLARE.getWalletId(), new String[]{Coins.SOL.coinId()}, true, true, true),
        SENDER(Wallet.SENDER.getWalletId(), new String[]{Coins.NEAR.coinId()}, true, false, true),
        POLKADOT(Wallet.POLKADOTJS.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true),
        SUBWALLET(Wallet.SUBWALLET.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true),
        DEFAULT("default", new String[]{}, false, false, true),
        ;

        private String walletId;

        public String[] getCoinIds() {
            return coinIds;
        }

        private String[] coinIds;
        private boolean showChangePath;
        private boolean showSelectAddress;
        private boolean showTutorial;

        WalletConfig(String walletId, String[] coinIds, boolean showChangePath, boolean showSelectAddress, boolean showTutorial) {
            this.walletId = walletId;
            this.coinIds = coinIds;
            this.showChangePath = showChangePath;
            this.showSelectAddress = showSelectAddress;
            this.showTutorial = showTutorial;
        }

        public static WalletConfig getConfigByWalletId(String walletId) {
            Optional<WalletConfig> config = Arrays.stream(WalletConfig.values()).filter(assetConfig -> assetConfig.walletId.equals(walletId)).findFirst();
            return config.orElse(DEFAULT);
        }


        public String getWalletId() {
            return walletId;
        }

        public boolean isShowChangePath() {
            return showChangePath;
        }

        public boolean isShowSelectAddress() {
            return showSelectAddress;
        }

        public boolean isShowTutorial() {
            return showTutorial;
        }

        public String getCoinId() {
            // should determine which coinId to use when it is a multi_chain wallet;
            return coinIds[0];
        }
    }
}
