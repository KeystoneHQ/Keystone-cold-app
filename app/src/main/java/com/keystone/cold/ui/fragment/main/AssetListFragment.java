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

import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.Constants.KEY_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.CUSTOM_CHAIN_IDENTIFIER;
import static com.keystone.cold.ui.fragment.main.AssetFragment.DATA_TYPE;
import static com.keystone.cold.ui.fragment.main.AssetFragment.HD_PATH;
import static com.keystone.cold.ui.fragment.main.AssetFragment.REQUEST_ID;
import static com.keystone.cold.ui.fragment.main.AssetFragment.SIGN_DATA;
import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;
import static com.keystone.cold.ui.fragment.main.polkadot.PolkadotTxConfirm.KEY_PARSED_TRANSACTION;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;
import static com.keystone.cold.viewmodel.CoinListViewModel.coinEntityComparator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
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

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.exception.CoinNotFindException;
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
import com.keystone.cold.protocol.ZipUtil;
import com.keystone.cold.protocol.parser.ProtoParser;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.ui.modal.PolkadotErrorDialog;
import com.keystone.cold.util.StepFragmentHelper;
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
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignRequest;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
                WatchWallet.getWatchWallet(mActivity) == WatchWallet.ARConnect ||
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
        if (watchWallet.equals(WatchWallet.CORE_WALLET) || watchWallet.equals(WatchWallet.BIT_KEEP) || watchWallet.equals(WatchWallet.KEPLR_WALLET)) {
            mBinding.toolbar.setTitle(R.string.select_network);
        }
    }

    private void showBadge(MenuItem menuItem) {
        Drawable menu = Objects.requireNonNull(menuItem).getIcon();
        int badgeSize = (int) getResources().getDimension(R.dimen.default_badge_size);
        Drawable menuWithBadge = ViewUtils.addBadge(getResources(), menu, badgeSize);
        menuItem.setIcon(menuWithBadge);
    }

    private BadgeView generateBadgeView(View anchor) {
        return BadgeFactory.create(anchor.getContext())
                .setWidthAndHeight(10, 10)
                .setBadgeBackground(Color.RED)
                .setBadgeGravity(Gravity.END | Gravity.TOP)
                .setShape(BadgeView.SHAPE_CIRCLE)
                .setSpace(10, 0)
                .bind(anchor);
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
                    if (watchWallet != WatchWallet.KEPLR_WALLET) {
                        toShow.sort(coinEntityComparator);
                    }
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
        if (watchWallet != WatchWallet.KEYSTONE && watchWallet != WatchWallet.CORE_WALLET && watchWallet != WatchWallet.BIT_KEEP && watchWallet != WatchWallet.KEPLR_WALLET) {
            MenuItem item = menu.findItem(R.id.action_more);
            item.setVisible(false);
        }
        if (judgeShowBadge()) {
            MenuItem menuItem = menu.findItem(R.id.action_more);
            showBadge(menuItem);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            if (watchWallet.equals(WatchWallet.KEYSTONE) || watchWallet.equals(WatchWallet.CORE_WALLET) || watchWallet == WatchWallet.BIT_KEEP || watchWallet.equals(WatchWallet.KEPLR_WALLET)) {
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
                StepFragmentHelper.getInstance().setStartingPoint(AssetListFragment.class.getName());
                if (result.getType().equals(ScanResultTypes.UR_CRYPTO_PSBT)) {
                    handleCryptoPSBT(result);
                } else if (result.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
                    handleETHSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_COSMOS_SIGN_REQUEST)) {
                    handleCosmosSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UOS)) {
                    handleUOS(result);
                } else if (result.getType().equals(ScanResultTypes.UR_BYTES)) {
                    handleURBytes(result);
                } else if (result.getType().equals(ScanResultTypes.UR_EVM_SIGN_REQUEST)) {
                    handleEvmSignRequest(result);
                } else {
                    throw new UnknowQrCodeException("unknown transaction!");
                }
            }

            private void handleURBytes(ScanResult result) throws UnknowQrCodeException, XfpNotMatchException, InvalidTransactionException, JSONException, CoinNotFindException {
                String hex = Hex.toHexString((byte[]) result.resolve());
                //xrp toolkit tx, metamask tx, webauth tx, decode as JSON
                JSONObject object = tryDecodeAsJson(hex);

                //keystone, try decode as protobuf
                if (object == null) {
                    object = tryDecodeAsProtobuf(hex);
                }

                if (object != null) {
                    decodeAndProcess(object);
                } else {
                    throw new UnknowQrCodeException("unknown UR qr code");
                }
            }

            private boolean checkWebAuth(JSONObject object) throws JSONException {
                JSONObject webAuth = object.optJSONObject("data");
                if (webAuth != null && webAuth.optString("type").equals("webAuth")) {
                    String data = object.getString("data");
                    Bundle bundle = new Bundle();
                    bundle.putString(WEB_AUTH_DATA, data);
                    bundle.putBoolean(IS_SETUP_VAULT, false);
                    mFragment.navigate(R.id.action_QRCodeScan_to_result, bundle);
                    return true;
                }
                return false;
            }

            private void decodeAndProcess(JSONObject object)
                    throws
                    CoinNotFindException,
                    JSONException,
                    XfpNotMatchException, UnknowQrCodeException, InvalidTransactionException {
                if (checkWebAuth(object)) return;
                if (object.optString("type").equals("TYPE_SIGN_TX")) {
                    handleSign(object);
                    return;
                }
                throw new UnknowQrCodeException("unknown qr code type");
            }

            private void handleSign(JSONObject object)
                    throws InvalidTransactionException,
                    CoinNotFindException,
                    XfpNotMatchException {
                String xfp = new GetMasterFingerprintCallable().call();
                if (!object.optString("xfp").equals(xfp)) {
                    throw new XfpNotMatchException("xfp not match");
                }
                try {
                    JSONObject transaction = object.getJSONObject("signTx");
                    String coinCode = transaction.getString("coinCode");
                    if (!WatchWallet.isSupported(mActivity.getApplication(), coinCode) || transaction.has("omniTx")) {
                        throw new CoinNotFindException("not support " + coinCode);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TX_DATA, object.getJSONObject("signTx").toString());
                    mFragment.navigate(R.id.action_to_txConfirmFragment, bundle);
                } catch (JSONException e) {
                    throw new InvalidTransactionException("invalid transaction");
                }
            }

            private JSONObject tryDecodeAsJson(String hex) {
                try {
                    return new JSONObject(new String(Hex.decode(hex)));
                } catch (Exception ignored) {
                }
                return null;
            }

            private JSONObject tryDecodeAsProtobuf(String hex) {
                JSONObject object;
                hex = ZipUtil.unzip(hex);
                object = new ProtoParser(Hex.decode(hex)).parseToJson();
                return object;
            }

            private void handleUOS(ScanResult result) {
                try {
                    String res = result.getData();
                    PolkadotViewModel polkadotViewModel = ViewModelProviders.of(mFragment).get(PolkadotViewModel.class);
                    JSONObject json = polkadotViewModel.parseTransaction(res);
                    String type = json.getString("transaction_type");
                    JSONArray content = json.getJSONArray("content");
                    if (type.equals("Read")) {
                        PolkadotErrorDialog.show(mActivity, getString(R.string.notice), getString(R.string.decline), content, () -> mFragment.navigateUp());
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_TX_DATA, res);
                        bundle.putString(KEY_PARSED_TRANSACTION, json.toString());
                        mFragment.navigate(R.id.action_to_polkadotTxConfirm, bundle);
                    }
                } catch (Exception e) {
                    handleException(e);
                }
            }

            private void handleCosmosSignRequest(ScanResult result) throws XfpNotMatchException, InvalidTransactionException {
                CosmosSignRequest cosmosSignRequest = (CosmosSignRequest) result.resolve();
                ByteBuffer uuidBuffer = ByteBuffer.wrap(cosmosSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = cosmosSignRequest.getDerivationPath();
                String requestMFP = Hex.toHexString(cosmosSignRequest.getMasterFingerprint());
                String signData = Hex.toHexString(cosmosSignRequest.getSignData());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                Bundle bundle = new Bundle();
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putString(SIGN_DATA, signData);
                bundle.putString(HD_PATH, "M/" + hdPath);
                String dataType = cosmosSignRequest.getType().getType();
                if (dataType.equals(CosmosSignRequest.DataType.AMINO.getType()) || dataType.equals(CosmosSignRequest.DataType.DIRECT.getType())) {
                    bundle.putString(DATA_TYPE, cosmosSignRequest.getType().getType());
                    mFragment.navigate(R.id.action_to_cosmosTxConfirmFragment, bundle);
                } else if (dataType.equals(CosmosSignRequest.DataType.MESSAGE.getType())) {
                    mFragment.navigate(R.id.action_to_cosmosMessageFragment, bundle);
                } else {
                    throw new InvalidTransactionException("The textual format is not supported");
                }
            }

            private void handleEvmSignRequest(ScanResult result) throws XfpNotMatchException, InvalidTransactionException {
                EvmSignRequest evmSignRequest = (EvmSignRequest) result.resolve();
                String requestMFP = Hex.toHexString(evmSignRequest.getMasterFingerprint());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                ByteBuffer uuidBuffer = ByteBuffer.wrap(evmSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = evmSignRequest.getDerivationPath();
                String signData = Hex.toHexString(evmSignRequest.getSignData());

                Bundle bundle = new Bundle();
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putString(SIGN_DATA, signData);
                bundle.putString(HD_PATH, "M/" + hdPath);
                String dataType = evmSignRequest.getDataType();
                if (dataType.equals(EvmSignRequest.DataType.AMINO_TRANSACTION.getType())
                        || dataType.equals(EvmSignRequest.DataType.DIRECT_TRANSACTION.getType())) {
                    bundle.putString(DATA_TYPE, dataType);
                    bundle.putLong(CUSTOM_CHAIN_IDENTIFIER, evmSignRequest.getCustomChainIdentifier());
                    mFragment.navigate(R.id.action_to_cosmosTxConfirmFragment, bundle);
                } else {
                    throw new InvalidTransactionException("The format is not supported");
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
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_CRYPTO_PSBT, ScanResultTypes.UR_ETH_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.BIT_KEEP) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_CRYPTO_PSBT, ScanResultTypes.UR_ETH_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.KEPLR_WALLET) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_COSMOS_SIGN_REQUEST, ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_EVM_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.KEYSTONE) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_BYTES, ScanResultTypes.UOS));
        }
        scannerState.setDesiredResults(desiredResults);
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(scannerState);
        navigate(R.id.action_to_scanner);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        if (watchWallet.equals(WatchWallet.CORE_WALLET) || watchWallet.equals(WatchWallet.BIT_KEEP) || watchWallet.equals(WatchWallet.KEPLR_WALLET)) {
            DialogBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.dialog_bottom_sheet, null, false);
            binding.addAddress.setVisibility(View.GONE);
            binding.resetDb.setVisibility(View.GONE);
            binding.changePath.setVisibility(View.GONE);
            binding.searchAddress.setVisibility(View.GONE);
            binding.exportXpub.setVisibility(View.GONE);
            binding.tutorials.setOnClickListener(v -> {
                navigate(R.id.action_to_tutorialsFragment);
                dialog.dismiss();
            });
            if (judgeShowBadge()) {
                generateBadgeView(binding.syncText);
            }

            binding.sync.setOnClickListener(v -> {
                if (judgeShowBadge()) {
                    setUserClickSyncLock();
                }
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

    private boolean judgeShowBadge() {
        boolean isShowBadge = false;
        if (watchWallet == WatchWallet.CORE_WALLET && !Utilities.hasUserClickCoreWalletSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.KEPLR_WALLET && !Utilities.hasUserClickKeplrSyncLock(mActivity)) {
            isShowBadge = true;
        }
        return isShowBadge;
    }

    private void setUserClickSyncLock() {
        switch (watchWallet) {
            case CORE_WALLET:
                Utilities.setUserClickCoreWalletSyncLock(mActivity);
                break;
            case KEPLR_WALLET:
                Utilities.setUserClickKeplrSyncLock(mActivity);
                break;
            default:
                break;
        }
    }

    private final CoinClickCallback mCoinClickCallback = coin -> {
        Bundle data = new Bundle();
        data.putLong(KEY_ID, coin.getId());
        data.putString(KEY_COIN_ID, coin.getCoinId());
        data.putString(KEY_COIN_CODE, coin.getCoinCode());
        navigate(R.id.action_to_assetFragment, data);
    };
}
