package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.accounts.BTCAccount;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.DialogAssetBottomBinding;
import com.keystone.cold.databinding.FragmentSyncBinding;
import com.keystone.cold.databinding.ProgressModalBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.ui.ConnectWalletActivity;
import com.keystone.cold.remove_wallet_mode.ui.MainActivity;
import com.keystone.cold.remove_wallet_mode.ui.SetupVaultActivity;
import com.keystone.cold.remove_wallet_mode.ui.adapter.SupportNetworkAdapter;
import com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet.config.WalletConfig;
import com.keystone.cold.remove_wallet_mode.viewmodel.CardanoViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.BitKeepWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.BlueWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.CoreWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.FewchaWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeplrWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeyRequestViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.KeystoneViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.MetamaskViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.OKXWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SenderWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SolFlareViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.SubstrateWalletViewModel;
import com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel.XRPToolkitViewModel;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.sparrowwallet.hummingbird.UR;

import java.util.Arrays;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SyncFragment extends BaseFragment<FragmentSyncBinding> {
    public enum SyncActionMode {
        Normal,
        KeyRequest;

        public static SyncActionMode fromOrdinary(int value) {
            switch (value) {
                case 1:
                    return KeyRequest;
                default:
                    return Normal;
            }
        }
    }

    private Wallet wallet;
    private List<Long> addressIds;
    private SyncActionMode syncActionMode;
    private String coinId;

    @Override
    protected int setView() {
        return R.layout.fragment_sync;
    }

    private void finish() {
        if (mActivity != null && mActivity instanceof SetupVaultActivity) {
            startActivity(new Intent(mActivity, MainActivity.class));
        }
        if (mActivity instanceof ConnectWalletActivity) {
            mActivity.finish();
        }
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).restart();
        }
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        Bundle data = getArguments();
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);
        syncActionMode = SyncActionMode.fromOrdinary(data.getInt(BundleKeys.SYNC_ACTION_MODE_KEY, 0));
        if (data.containsKey(BundleKeys.ADDRESS_IDS_KEY)) {
            addressIds = (List<Long>) data.getSerializable(BundleKeys.ADDRESS_IDS_KEY);
        }
        wallet = Wallet.getWalletById(walletId);
        mBinding.setWallet(wallet.getWalletName());
        mBinding.info.setOnClickListener((v) -> {
            toTutorial(null);
        });

        mBinding.complete.setOnClickListener(v -> finish());
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
                if (openCoins != null) {
                    WalletConfig.setOpenCoins(openCoins);
                }
                {
                    mBinding.dynamicQrcodeLayout.llQrHint.setVisibility(View.GONE);
                    mBinding.dynamicQrcodeLayout.llSupportCoins.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mBinding.dynamicQrcodeLayout.rvSupportCoinList.setLayoutManager(layoutManager);
                    SupportNetworkAdapter supportNetworkAdapter = new SupportNetworkAdapter(getContext());
                    supportNetworkAdapter.setItems(Arrays.asList(
                            R.drawable.ic_coin_btc,
                            R.drawable.ic_coin_eth,
                            R.drawable.ic_coin_bnb,
                            R.drawable.ic_coin_arb,
                            R.drawable.ic_coin_avax,
                            R.drawable.ic_coin_matic,
                            R.drawable.ic_coin_okb,
                            R.drawable.ic_more_drak
                    ));
                    mBinding.dynamicQrcodeLayout.rvSupportCoinList.setAdapter(supportNetworkAdapter);
                }
                break;
            case OKX:
                mBinding.dynamicQrcodeLayout.llQrHint.setVisibility(View.GONE);
                mBinding.dynamicQrcodeLayout.llSupportCoins.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                mBinding.dynamicQrcodeLayout.rvSupportCoinList.setLayoutManager(layoutManager);
                SupportNetworkAdapter supportNetworkAdapter = new SupportNetworkAdapter(getContext());
                supportNetworkAdapter.setItems(Arrays.asList(
                        R.drawable.ic_coin_btc,
                        R.drawable.ic_coin_eth,
                        R.drawable.ic_coin_okb,
                        R.drawable.ic_coin_bnb,
                        R.drawable.ic_coin_arb,
                        R.drawable.ic_coin_avax,
                        R.drawable.ic_coin_matic,
                        R.drawable.ic_more_drak
                ));
                mBinding.dynamicQrcodeLayout.rvSupportCoinList.setAdapter(supportNetworkAdapter);
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
        switch (syncActionMode) {
            case Normal: {
                switch (wallet) {
                    case POLKADOTJS:
                    case SUBWALLET:
                        generateSubstrateSyncData();
                        break;
                    default:
                        generateURSyncData();
                }
                break;
            }
            case KeyRequest: {
                generateKeyRequestSyncData();
                break;
            }
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
            case OKX:
                OKXWalletViewModel okxWalletViewModel = ViewModelProviders.of(this).get(OKXWalletViewModel.class);
                okxWalletViewModel.setOpenedCoins((List<String>) requireArguments().getSerializable(BundleKeys.OPENED_COINS_KEY));
                urMutableLiveData = okxWalletViewModel.generateSyncUR();
                break;
            case FEWCHA:
            case PETRA:
            case SUIET:
                FewchaWalletViewModel fewchaWalletViewModel = ViewModelProviders.of(this).get(FewchaWalletViewModel.class);
                coinId = requireArguments().getString(BundleKeys.COIN_ID_KEY);
                fewchaWalletViewModel.setCoinId(coinId);
                fewchaWalletViewModel.setAddressIds(addressIds);
                urMutableLiveData = fewchaWalletViewModel.generateSyncUR();
                break;
            case RABBY:
            case SAFE:
            case ZAPPER:
            case YEARN:
            case SUSHISWAP:
            case BLOCKWALLET:
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

    private void setupKeyRequestUR(List<KeyRequestApproveFragment.Schema> schemas, @Nullable String password) {
        KeyRequestViewModel keyRequestViewModel = ViewModelProviders.of(this).get(KeyRequestViewModel.class);
        LiveData<UR> urMutableLiveData = keyRequestViewModel.generateSyncUR(schemas, password);
        urMutableLiveData.observe(this, ur -> {
            if (ur != null) {
                mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                urMutableLiveData.removeObservers(this);
            }
        });
    }

    private void generateKeyRequestSyncData() {
        KeyRequestViewModel keyRequestViewModel = ViewModelProviders.of(this).get(KeyRequestViewModel.class);
        Bundle data = requireArguments();
        KeyRequestApproveFragment.KeyDerivationRequest request = (KeyRequestApproveFragment.KeyDerivationRequest) data.getSerializable(BundleKeys.KEY_REQUEST_KEY);
        LiveData<Boolean> needPassword = keyRequestViewModel.checkNeedPassword(request.getSchemas());

        needPassword.observe(this, (v) -> {
            if (v == null) return;
            if (!v) {
                setupKeyRequestUR(request.getSchemas(), null);
                needPassword.removeObservers(this);
            }
            if (v) {
                AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), getString(R.string.key_request_with_password), (password) -> {
                    setupKeyRequestUR(request.getSchemas(), password.password);
                }, this::finish, null);
                needPassword.removeObservers(this);
            }
        });
    }


    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogAssetBottomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_asset_bottom, null, false);
        WalletConfig config = WalletConfig.getConfigByWalletId(wallet.getWalletId());
        if (config.isShowSelectAddress()) {
            if (wallet.equals(Wallet.POLKADOTJS) || wallet.equals(Wallet.SUBWALLET) || wallet.equals(Wallet.XRPTOOLKIT)) {
                binding.selectAccountText.setText(R.string.import_another_account);
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
                bundle.putString(BundleKeys.PAGE_TITLE_KEY, getString(R.string.import_another_account));
                if (wallet.equals(Wallet.XRPTOOLKIT)) {
                    bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY, Coins.XRP.coinId()));
                } else {
                    bundle.putString(BundleKeys.COIN_ID_KEY, requireArguments().getString(BundleKeys.COIN_ID_KEY));
                }
                navigate(R.id.action_to_selectOneAddressFragment, bundle);
            } else {
                navigateUp();
                Bundle bundle = new Bundle();
                if (!TextUtils.isEmpty(coinId)) {
                    bundle.putString(BundleKeys.COIN_ID_KEY, coinId);
                } else {
                    bundle.putString(BundleKeys.COIN_ID_KEY, config.getCoinId());
                }
                bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
                navigate(R.id.action_to_selectAddressFragment, bundle);
            }
            dialog.dismiss();
        });
        binding.rlTutorial.setOnClickListener(v -> {
            toTutorial(dialog::dismiss);
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

    private void toTutorial(Runnable callback) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
        navigate(R.id.action_to_tutorialsFragment, bundle);
        if (callback != null) callback.run();
    }
}
