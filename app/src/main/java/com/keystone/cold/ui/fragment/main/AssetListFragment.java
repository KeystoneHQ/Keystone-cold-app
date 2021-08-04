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

import android.content.Context;
import android.content.Intent;
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

import com.keystone.coinlib.exception.CoinNotFindException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.AssetListBottomMenuBinding;
import com.keystone.cold.databinding.AssetListFragmentBinding;
import com.keystone.cold.db.PresetData;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.protocol.ZipUtil;
import com.keystone.cold.protocol.parser.ProtoParser;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.ui.fragment.main.scan.scanner.exceptions.UnExpectedQRException;
import com.keystone.cold.viewmodel.CoinListViewModel;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.Constants.KEY_ID;
import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;
import static com.keystone.cold.viewmodel.CoinListViewModel.coinEntityComparator;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class AssetListFragment extends BaseFragment<AssetListFragmentBinding> {

    public static final String TAG = "AssetListFragment";

    private CoinAdapter mCoinAdapter;
    private WatchWallet watchWallet;


    @Override
    protected int setView() {
        return R.layout.asset_list_fragment;
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT ||
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.METAMASK) {
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
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinListViewModel mViewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        subscribeUi(mViewModel.getCoins());
        checkAndAddNewCoins();
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
        if (watchWallet != WatchWallet.KEYSTONE) {
            MenuItem item = menu.findItem(R.id.action_more);
            item.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            AndPermission.with(this)
                    .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                    .onGranted(permissions -> {
                        scanQRCode();
                    })
                    .onDenied(permissions -> {
                        Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(mActivity, getString(R.string.scan_permission_denied), Toast.LENGTH_LONG).show();
                    }).start();
            return true;
        }

        if (id == R.id.action_more) {
            showBottomSheetMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanQRCode() {
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(new ScannerState(Collections.singletonList(ScanResultTypes.UR_BYTES)) {
            @Override
            public void handleScanResult(ScanResult result) throws Exception {
                if (result.getType().equals(ScanResultTypes.UR_BYTES)) {
                    if (handleWebAuth(result)) return;
                    if (handleKeystoneTx(result)) return;
                    throw new UnExpectedQRException("cannot resolve ur bytes");
                }
            }

            @Override
            public boolean handleException(Exception e) {
                e.printStackTrace();
                if (e instanceof XfpNotMatchException) {
                    mFragment.alert(getString(R.string.account_not_match), getString(R.string.account_not_match_detail));
                    return true;
                } else if (e instanceof InvalidTransactionException) {
                    mFragment.alert(getString(R.string.unresolve_tx),
                            getString(R.string.unresolve_tx_hint,
                                    WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
                    return true;
                } else if (e instanceof CoinNotFindException) {
                    mFragment.alert(null, getString(R.string.unsupported_coin), null);
                    return true;
                }
                return false;
            }

            private boolean handleWebAuth(ScanResult result) throws JSONException {
                JSONObject object = new JSONObject(new String((byte[]) result.resolve(), StandardCharsets.UTF_8));
                JSONObject webAuth = object.optJSONObject("data");
                if (webAuth != null && webAuth.optString("type").equals("webAuth")) {
                    String data = webAuth.getString("data");
                    Bundle bundle = new Bundle();
                    bundle.putString(WEB_AUTH_DATA, data);
                    bundle.putBoolean(IS_SETUP_VAULT, false);
                    mFragment.navigate(R.id.action_QRCodeScan_to_result, bundle);
                    return true;
                }
                return false;
            }

            private boolean handleKeystoneTx(ScanResult result) throws XfpNotMatchException, UnknowQrCodeException, JSONException, CoinNotFindException, InvalidTransactionException {
                byte[] bytes = (byte[]) result.resolve();
                String hex = Hex.toHexString(bytes);
                JSONObject object;
                hex = ZipUtil.unzip(hex);
                object = new ProtoParser(Hex.decode(hex)).parseToJson();
                if (object != null) {
                    Log.i(TAG, "decodeAsProtobuf result: " + object);
                    WatchWallet wallet = WatchWallet.getWatchWallet(MainApplication.getApplication());
                    if (wallet != WatchWallet.KEYSTONE) {
                        throw new UnknowQrCodeException("not support bc32 qrcode in current wallet mode");
                    }
                    String type = object.getString("type");
                    if ("TYPE_SIGN_TX".equals(type)) {
                        String xfp = new GetMasterFingerprintCallable().call();
                        if (!object.optString("xfp").equals(xfp)) {
                            throw new XfpNotMatchException("xfp not match");
                        }
                        try {
                            JSONObject transaction = object.getJSONObject("signTx");
                            String coinCode = transaction.getString("coinCode");
                            if (!WatchWallet.isSupported(mActivity, coinCode)) {
                                throw new CoinNotFindException("not support " + coinCode);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString(KEY_TX_DATA, object.getJSONObject("signTx").toString());
                            mFragment.navigate(R.id.action_to_txConfirmFragment, bundle);
                            return true;
                        } catch (JSONException e) {
                            throw new InvalidTransactionException("invalid transaction");
                        }
                    }
                    throw new UnknowQrCodeException("unknow qrcode type " + type);
                }
                return false;
            }
        });
        navigate(R.id.action_to_scanner);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
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
