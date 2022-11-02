/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.ui.fragment.main;

import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.Constants.KEY_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;
import static com.keystone.cold.viewmodel.CoinListViewModel.coinEntityComparator;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.exception.InvalidETHAccountException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.AssetListBottomMenuBinding;
import com.keystone.cold.databinding.AssetListFragmentBinding;
import com.keystone.cold.databinding.DialogBottomSheetBinding;
import com.keystone.cold.db.PresetData;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.util.ViewUtils;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;
import com.keystone.cold.viewmodel.tx.psbt.PSBTViewModel;
import com.keystone.cold.viewmodel.PolkadotViewModel;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssetListFragment extends BaseFragment<AssetListFragmentBinding> {

    public static final String TAG = "AssetListFragment";

    private CoinAdapter mCoinAdapter;
    private WatchWallet watchWallet;
    private DataRepository mRepository;


    @Override
    protected int setView() {
        return R.layout.asset_list_fragment;
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.METAMASK ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.SOLANA ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.NEAR ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.APTOS) {
            navigate(R.id.assetFragment);
        }
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
        mCoinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, false);
        mBinding.assetList.setAdapter(mCoinAdapter);
        mRepository = ((MainApplication) mActivity.getApplication()).getRepository();
        if (watchWallet.equals(WatchWallet.POLKADOT_JS)) {
            mBinding.hint.setText(R.string.polkadot_hint);
            mBinding.hint.setVisibility(View.VISIBLE);
        } else {
            mBinding.hint.setVisibility(View.GONE);
        }
        if (watchWallet.equals(WatchWallet.CORE_WALLET)) {
            mBinding.toolbar.setTitle(R.string.select_network);
            MenuItem menuItem = mBinding.toolbar.getMenu().findItem(R.id.action_more);
            if (!Utilities.hasUserClickCoreWalletSyncLock(mActivity)) {
                showBadge(menuItem);
            }
        }
    }

    private void showBadge(MenuItem menuItem) {
        Drawable menu = Objects.requireNonNull(menuItem).getIcon();
        int badgeSize = (int) getResources().getDimension(R.dimen.default_badge_size);
        Drawable menuWithBadge = ViewUtils.addBadge(getResources(), menu, badgeSize);
        menuItem.setIcon(menuWithBadge);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinListViewModel mViewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        subscribeUi(mViewModel.getCoins());
        checkAndAddNewCoins();
        if (watchWallet == WatchWallet.POLKADOT_JS) {
            PolkadotViewModel polkadotViewModel = ViewModelProviders.of(this).get(PolkadotViewModel.class);
            try {
                polkadotViewModel.initialDB();
                LiveData<List<AddressEntity>> dot = mRepository.loadAddress(Coins.DOT.coinId());
                dot.observe(this, addressEntities -> addressEntities.forEach((a) -> {
                    try {
                        byte[] pubkey = AddressCodec.decodeAddress(a.getAddressString());
                        String publicKey = Hex.toHexString(pubkey);
                        String path = a.getPath();
                        polkadotViewModel.importAddress(publicKey, path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                LiveData<List<AddressEntity>> ksm = mRepository.loadAddress(Coins.KSM.coinId());
                ksm.observe(this, addressEntities -> addressEntities.forEach((a) -> {
                    try {
                        byte[] pubkey = AddressCodec.decodeAddress(a.getAddressString());
                        String publicKey = Hex.toHexString(pubkey);
                        String path = a.getPath();
                        polkadotViewModel.importAddress(publicKey, path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            } catch (PolkadotViewModel.PolkadotException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAndAddNewCoins() {
        SetupVaultViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(SetupVaultViewModel.class);
        AppExecutors.getInstance().diskIO().execute(()
                -> viewModel.presetData(PresetData.generateCoins(mActivity), null)
        );
    }

    private void subscribeUi(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, coinEntities -> {
            if (coinEntities != null) {
                List<CoinEntity> toShow = filterDisplayCoins(coinEntities);
                if (toShow.isEmpty()) {
                    mBinding.setIsEmpty(true);
                } else {
                    mBinding.setIsEmpty(false);
                    toShow.sort(coinEntityComparator);
                    mCoinAdapter.setItems(toShow);
                }

            } else {
                mBinding.setIsEmpty(true);
            }
            mBinding.executePendingBindings();
        });
    }

    private List<CoinEntity> filterDisplayCoins(List<CoinEntity> coinEntities) {
        Stream<CoinEntity> filterStream = filterSupportedCoin(coinEntities, watchWallet);
        if (watchWallet == WatchWallet.KEYSTONE) {
            filterStream = filterStream.filter(CoinEntity::isShow);
        }
        return filterStream.collect(Collectors.toList());
    }

    public static Stream<CoinEntity> filterSupportedCoin(List<CoinEntity> coinEntities, WatchWallet watchWallet) {
        Coins.Coin[] supportedCoins = watchWallet.getSupportedCoins();
        return coinEntities.stream()
                .filter(c -> Arrays.stream(supportedCoins)
                        .anyMatch(coin -> coin.coinCode().equals(c.getCoinCode()))
                );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        if (watchWallet != WatchWallet.KEYSTONE && watchWallet != WatchWallet.CORE_WALLET) {
            MenuItem item = menu.findItem(R.id.action_more);
            item.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            if (watchWallet.equals(WatchWallet.CORE_WALLET)) {
                scanQrCode();
            } else {
                AndPermission.with(this)
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .onGranted(permissions -> navigate(R.id.action_to_scan))
                        .onDenied(permissions -> {
                            Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Toast.makeText(mActivity, getString(R.string.scan_permission_denied), Toast.LENGTH_LONG).show();
                        }).start();
            }
            return true;
        }

        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanQrCode() {
        ScannerState scannerState = new ScannerState() {
            @Override
            public void handleScanResult(ScanResult result) throws Exception {
                if (result.getType().equals(ScanResultTypes.UR_CRYPTO_PSBT)) {
                    handleCryptoPSBT(result);
                } else if (result.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
                    handleETHSignRequest(result);
                } else {
                    throw new UnknowQrCodeException("unknown transaction!");
                }
            }

            private void handleCryptoPSBT(ScanResult result) throws InvalidTransactionException {
                CryptoPSBT cryptoPSBT = (CryptoPSBT) result.resolve();
                byte[] bytes = cryptoPSBT.getPsbt();
                String psbtB64 = Base64.toBase64String(bytes);
                PSBTViewModel psbtViewModel = ViewModelProviders.of(mFragment).get(PSBTViewModel.class);
                String myMasterFingerprint = new GetMasterFingerprintCallable().call();
                psbtViewModel.parsePsbtBase64(psbtB64, myMasterFingerprint);
                Bundle data = new Bundle();
                data.putString("psbt", psbtB64);
                mFragment.navigate(R.id.action_to_psbtConfirmFragment, data);
            }

            private void handleETHSignRequest(ScanResult result) throws InvalidTransactionException, InvalidETHAccountException, XfpNotMatchException, UnknowQrCodeException {
                EthSignRequest ethSignRequest = (EthSignRequest) result.resolve();
                Bundle bundle = new Bundle();
                ByteBuffer uuidBuffer = ByteBuffer.wrap(ethSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = ethSignRequest.getDerivationPath();
                String requestMFP = Hex.toHexString(ethSignRequest.getMasterFingerprint());
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putString(SIGN_DATA, Hex.toHexString(ethSignRequest.getSignData()));
                bundle.putString(HD_PATH, "M/" + hdPath);

                ETHAccount current = ETHAccount.ofCode(Utilities.getCurrentEthAccount(mFragment.getActivity()));
                ETHAccount target = ETHAccount.getAccountByPath(hdPath);
                if (target == null) {
                    throw new InvalidTransactionException("This transaction use an invalid key path");
                }
                if (!target.equals(current)) {
                    if (!current.isChildrenPath(hdPath)) {
                        //standard and ledger_live has overlap of 1st address
                        throw new InvalidETHAccountException("not expected ETH account", current, target);
                    }
                }

                String MFP = new GetMasterFingerprintCallable().call();

                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TRANSACTION.getType())) {
                    mFragment.navigate(R.id.action_to_ethTxConfirmFragment, bundle);
                } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_DATA.getType())) {
                    mFragment.navigate(R.id.action_to_ethSignTypedDataFragment, bundle);
                } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.PERSONAL_MESSAGE.getType())) {
                    mFragment.navigate(R.id.action_to_ethSignMessageFragment, bundle);
                } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_TRANSACTION.getType())) {
                    byte[] typedTransaction = ethSignRequest.getSignData();
                    byte type = typedTransaction[0];
                    switch (type) {
                        case 0x02:
                            mFragment.navigate(R.id.action_to_ethFeeMarketTxConfirmFragment, bundle);
                            break;
                        default:
                            throw new UnknowQrCodeException("unknown transaction!");
                    }
                }

            }

            @Override
            public boolean handleException(Exception e) {
                e.printStackTrace();
                if (e instanceof InvalidTransactionException) {
                    mFragment.alert(getString(R.string.invalid_data), e.getMessage());
                    return true;
                } else if (e instanceof XfpNotMatchException) {
                    mFragment.alert(getString(R.string.account_not_match), getString(R.string.account_not_match_detail));
                    return true;
                } else if (e instanceof InvalidETHAccountException) {
                    mFragment.alertDoubleButtonModal(getString(R.string.invalid_data),
                            getString(R.string.invalid_account_tx, ((InvalidETHAccountException) e).getAccount().getName(), ((InvalidETHAccountException) e).getTarget().getName(), ((InvalidETHAccountException) e).getTarget().getName()),
                            getString(R.string.cancel),
                            getString(R.string.switch_wallet),
                            null, () -> {
                                Utilities.setCurrentEthAccount(mActivity, ((InvalidETHAccountException) e).getTarget().getCode());
                                popBackStack(R.id.assetFragment, false);
                            });
                    return true;
                }
                return false;
            }
        };

        List<ScanResultTypes> desiredResults = new ArrayList<>();
        if (watchWallet == WatchWallet.CORE_WALLET) {
            desiredResults.addAll(Collections.singletonList(ScanResultTypes.UR_CRYPTO_PSBT));
        }
        scannerState.setDesiredResults(desiredResults);
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(scannerState);
        navigate(R.id.action_to_scanner);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        if (watchWallet.equals(WatchWallet.CORE_WALLET)) {
            DialogBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.dialog_bottom_sheet, null, false);
            binding.addAddress.setVisibility(View.GONE);
            binding.resetDb.setVisibility(View.GONE);
            binding.changePath.setOnClickListener(v -> {
                navigate(R.id.action_assetListFragment_to_changeDerivePathFragment);
                dialog.dismiss();
            });
            binding.tutorials.setOnClickListener(v -> {
                navigate(R.id.action_to_tutorialsFragment);
                dialog.dismiss();
            });
            binding.sync.setOnClickListener(v -> {
                navigate(R.id.action_to_syncFragment);
                dialog.dismiss();
            });
            dialog.setContentView(binding.getRoot());
        } else {
            AssetListBottomMenuBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.asset_list_bottom_menu, null, false);
            binding.addHideAsset.setOnClickListener(v -> {
                navigate(R.id.action_to_manageCoinFragment);
                dialog.dismiss();

            });
            binding.sync.setOnClickListener(v -> {
                navigate(R.id.action_to_syncFragment);
                dialog.dismiss();

            });
            dialog.setContentView(binding.getRoot());
        }
        dialog.show();
    }

    private final CoinClickCallback mCoinClickCallback = coin -> {
        Bundle data = new Bundle();
        data.putLong(KEY_ID, coin.getId());
        data.putString(KEY_COIN_ID, coin.getCoinId());
        data.putString(KEY_COIN_CODE, coin.getCoinCode());
        navigate(R.id.action_to_assetFragment, data);
    };
}
