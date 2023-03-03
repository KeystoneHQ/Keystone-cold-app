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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentSyncBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.BitKeepWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.BlueWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.CoreWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.FewchaWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeplrWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeystoneViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.MetamaskViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SenderWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SolFlareViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SubstrateWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.XRPToolkitViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.sparrowwallet.hummingbird.UR;

import java.util.ArrayList;
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
        setupWalletUI(wallet);
        switchChainAccountsByNeeds(wallet);
    }

    public void switchChainAccountsByNeeds(Wallet wallet) {
        switch (wallet) {
            case KEYSTONE:
                Utilities.setCurrentBTCAccount(mActivity, BTCAccount.NATIVE_SEGWIT.getCode());
                Utilities.setCurrentEthAccount(mActivity, ETHAccount.BIP44_STANDARD.getCode());
                break;
            case CORE:
                Utilities.setCurrentEthAccount(mActivity, ETHAccount.BIP44_STANDARD.getCode());
                Utilities.setCurrentBTCAccount(mActivity, BTCAccount.CORE_NATIVE_SEGWIT.getCode());
                break;
        }
    }

    public void setupWalletUI(Wallet wallet) {
        switch (wallet) {
            case KEYSTONE:
                mBinding.hint.setText(R.string.scan_via_keystone);
                break;
            case CORE:
                mBinding.dynamicQrcodeLayout.llQrHint.setVisibility(View.VISIBLE);
                mBinding.dynamicQrcodeLayout.tvQrHint.setText(R.string.core_wallet_hint);
                break;
            case BITKEEP:
                Bundle bundle = requireArguments();
                List<String> openCoins = (List<String>) bundle.getSerializable(BundleKeys.OPENED_COINS_KEY);
                WalletConfig.setOpenCoins(openCoins);
                break;
        }
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
        LiveData<UR> urMutableLiveData;
        switch (wallet) {
            case KEYSTONE:
                KeystoneViewModel keystoneViewModel = ViewModelProviders.of(this).get(KeystoneViewModel.class);
                Bundle data = requireArguments();
                keystoneViewModel.setOpenedCoins((List<String>) data.getSerializable(BundleKeys.OPENED_COINS_KEY));
                urMutableLiveData = keystoneViewModel.generateSyncKeystone();
                break;
            case FEWCHA:
            case PETRA:
                FewchaWalletViewModel fewchaWalletViewModel = ViewModelProviders.of(this).get(FewchaWalletViewModel.class);
                fewchaWalletViewModel.setAddressIds(addressIds);
                urMutableLiveData = fewchaWalletViewModel.generateSyncUR();
                break;
            case RABBY:
            case SAFE:
            case ZAPPER:
            case YEARN:
            case SUSHISWAP:
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
            case ARCONNECT:
                ArweaveViewModel arweaveViewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
                urMutableLiveData = arweaveViewModel.generateSyncData();
                break;
            case KEPLR:
                KeplrWalletViewModel keplrWalletViewModel = ViewModelProviders.of(this).get(KeplrWalletViewModel.class);
                urMutableLiveData = keplrWalletViewModel.generateSyncUR();
                break;
            case CORE:
                CoreWalletViewModel coreWalletViewModel = ViewModelProviders.of(this).get(CoreWalletViewModel.class);
                urMutableLiveData = coreWalletViewModel.generateSyncUR();
                break;
            case XRPTOOLKIT:
                XRPToolkitViewModel xrpToolkitViewModel = ViewModelProviders.of(this).get(XRPToolkitViewModel.class);
                xrpToolkitViewModel.setAddressIds(addressIds);
                urMutableLiveData = xrpToolkitViewModel.generateSyncUR();
                break;
            case BITKEEP:
                BitKeepWalletViewModel bitKeepWalletViewModel = ViewModelProviders.of(this).get(BitKeepWalletViewModel.class);
                Bundle bundle = requireArguments();
                bitKeepWalletViewModel.setOpenedCoins((List<String>) bundle.getSerializable(BundleKeys.OPENED_COINS_KEY));
                urMutableLiveData = bitKeepWalletViewModel.generateSyncUR();
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
            if (wallet.equals(Wallet.POLKADOTJS) || wallet.equals(Wallet.SUBWALLET) || wallet.equals(Wallet.XRPTOOLKIT)) {
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
            if (wallet.equals(Wallet.POLKADOTJS) || wallet.equals(Wallet.SUBWALLET) || wallet.equals(Wallet.XRPTOOLKIT)) {
                navigateUp();
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
                if (wallet.equals(Wallet.XRPTOOLKIT)) {
                    bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY, Coins.XRP.coinId()));
                } else {
                    bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY));
                }
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
        RABBY(Wallet.RABBY.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        SAFE(Wallet.SAFE.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        ZAPPER(Wallet.ZAPPER.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        YEARN(Wallet.YEARN.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        SUSHISWAP(Wallet.SUSHISWAP.getWalletId(), new String[]{Coins.ETH.coinId()}, true, false, true),
        FEWCHA(Wallet.FEWCHA.getWalletId(), new String[]{Coins.APTOS.coinId()}, false, true, true),
        PETRA(Wallet.PETRA.getWalletId(), new String[]{Coins.APTOS.coinId()}, false, true, true),
        SOLFLARE(Wallet.SOLFLARE.getWalletId(), new String[]{Coins.SOL.coinId()}, true, true, true),
        SENDER(Wallet.SENDER.getWalletId(), new String[]{Coins.NEAR.coinId()}, true, false, true),
        POLKADOT(Wallet.POLKADOTJS.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true),
        SUBWALLET(Wallet.SUBWALLET.getWalletId(), new String[]{Coins.DOT.coinId(), Coins.KSM.coinId()}, false, true, true),
        XRPToolkit(Wallet.XRPTOOLKIT.getWalletId(), new String[]{Coins.XRP.coinId()}, false, true, true),
        BITKEEP(Wallet.BITKEEP.getWalletId(), new String[]{Coins.BTC.coinId(), Coins.ETH.coinId()}, true, false, true),
        BITKEEP_ONLY_BTC(Wallet.BITKEEP.getWalletId(), new String[]{Coins.BTC.coinId()}, false, false, true),
        DEFAULT("default", new String[]{}, false, false, true),
        ;

        private static final List<String> OPEN_COINS = new ArrayList<>();

        public static void setOpenCoins(List<String> openCoins) {
            OPEN_COINS.clear();
            OPEN_COINS.addAll(openCoins);
        }

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
            if (config.isPresent()) {
                if (config.get() == WalletConfig.BITKEEP) {
                    if (!OPEN_COINS.contains(Coins.ETH.coinId())) {
                        return BITKEEP_ONLY_BTC;
                    }
                }
                return config.get();
            }
            return DEFAULT;
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
            if (this == WalletConfig.BITKEEP) {
                return coinIds[1];
            }
            return coinIds[0];
        }
    }
}
