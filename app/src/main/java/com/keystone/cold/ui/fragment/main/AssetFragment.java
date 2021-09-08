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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.polkadot.UOS.Extrinsic;
import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.AssetFragmentBinding;
import com.keystone.cold.databinding.DialogBottomSheetBinding;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.ui.fragment.main.scan.scanner.exceptions.UnExpectedQRException;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.keystone.cold.viewmodel.CoinViewModel;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.keystone.cold.viewmodel.tx.PolkadotJsTxConfirmViewModel;
import com.keystone.cold.viewmodel.PublicKeyViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.exceptions.UnknownSubstrateChainException;
import com.keystone.cold.viewmodel.exceptions.UnsupportedSubstrateTxException;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;

import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;

public class AssetFragment extends BaseFragment<AssetFragmentBinding>
        implements Toolbar.OnMenuItemClickListener, NumberPickerCallback {

    public static final String TAG = "AssetFragment";

    private final ObservableField<String> query = new ObservableField<>();

    private boolean isInSearch;
    private Fragment[] fragments;
    private boolean showPublicKey;
    private String coinId;
    private String coinCode;
    private AddressNumberPicker mAddressNumberPicker;
    private boolean hasAddress;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.asset_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        if (watchWallet == WatchWallet.METAMASK) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.ETH.coinId();
            coinCode = Coins.ETH.coinCode();
        } else if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.XRP.coinId();
            coinCode = Coins.XRP.coinCode();
        } else {
            Bundle data = requireArguments();
            coinId = data.getString(KEY_COIN_ID);
            coinCode = data.getString(KEY_COIN_CODE);
            showPublicKey = Coins.showPublicKey(coinCode);
        }
        updateUI();
    }

    private void updateUI() {
        if (watchWallet == WatchWallet.POLKADOT_JS) {
            mBinding.button.setVisibility(View.VISIBLE);
            mBinding.button.setOnClickListener(v -> syncPolkadot());
        } else {
            mBinding.toolbar.inflateMenu(getMenuResId());
            mBinding.button.setVisibility(View.GONE);
        }
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (watchWallet == WatchWallet.XRP_TOOLKIT || watchWallet == WatchWallet.METAMASK) {
                ((MainActivity) mActivity).toggleDrawer(v);
            } else {
                navigateUp();
            }
        });
        initSearchView();
        initTabs();
    }

    private void syncPolkadot() {
        Bundle bundle = new Bundle();
        bundle.putString("coinCode", coinCode);
        navigate(R.id.action_to_syncFragment, bundle);
    }

    private int getMenuResId() {
        if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            return R.menu.xrp_toolkit;
        }
        if (watchWallet == WatchWallet.METAMASK) {
            return R.menu.metamask;
        }
        return (showPublicKey || Coins.isPolkadotFamily(coinCode)) ? R.menu.asset_without_add : R.menu.asset;
    }

    private void initTabs() {
        if (!showPublicKey) {
            initViewPager();
        } else {
            PublicKeyViewModel viewModel = ViewModelProviders.of(this)
                    .get(PublicKeyViewModel.class);
            Handler handler = new Handler();
            AppExecutors.getInstance().diskIO().execute(() -> {
                String address = viewModel.getAddress(coinId);
                hasAddress = !TextUtils.isEmpty(address);
                handler.post(this::initViewPager);
            });
        }

    }

    private void initViewPager() {
        String[] title = {showPublicKey && !hasAddress ? getString(R.string.tab_my_pubkey)
                : getString(R.string.tab_my_address),
                getString(R.string.tab_transaction_history)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            if (showPublicKey) {
                fragments[0] = PublicKeyFragment.newInstance(coinId);
            } else {
                fragments[0] = AddressFragment.newInstance(coinId, coinCode);
            }
            fragments[1] = TxListFragment.newInstance(coinId, coinCode);
        }

        mBinding.viewpager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
    }

    private void initSearchView() {
        mBinding.btnCancel.setOnClickListener(v -> exitSearch());
        View.OnKeyListener backListener = (view, key_code, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (key_code == KeyEvent.KEYCODE_BACK) {
                    if (isInSearch) {
                        exitSearch();
                        return true;
                    }
                }
            }
            return false;
        };
        mBinding.search.setOnKeyListener(backListener);
        query.set("");
        mBinding.setQuery(query);
        query.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (fragments[0] instanceof AddressFragment) {
                    AddressFragment addressFragment = (AddressFragment) fragments[0];
                    addressFragment.setQuery(query.get());
                }

                TxListFragment txListFragment = (TxListFragment) fragments[1];
                txListFragment.setQuery(query.get());

            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(), coinId);
        CoinViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(CoinViewModel.class);

        mBinding.setCoinViewModel(viewModel);
        subscribeUi(viewModel);
    }

    private void subscribeUi(CoinViewModel viewModel) {
        viewModel.getObservableCoin().observe(this, viewModel::setCoin);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                enterSearch();
                break;
            case R.id.action_add:
                handleAddAddress();
                break;
            case R.id.action_more:
                showBottomSheetMenu();
                break;
            case R.id.action_scan:
                if (watchWallet == WatchWallet.METAMASK || watchWallet == WatchWallet.POLKADOT_JS) {
                    scanQrCode();
                } else {
                    navigate(R.id.action_to_QRCodeScanFragment);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public static final String SIGN_DATA = "signData";
    public static final String REQUEST_ID = "requestId";
    public static final String HD_PATH = "hdPath";

    private void scanQrCode() {
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(new ScannerState(Arrays.asList(ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_BYTES, ScanResultTypes.UOS)) {
            @Override
            public void handleScanResult(ScanResult result) throws Exception {
                if (result.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
                    EthSignRequest ethSignRequest = (EthSignRequest) result.resolve();
                    Bundle bundle = new Bundle();
                    ByteBuffer uuidBuffer = ByteBuffer.wrap(ethSignRequest.getRequestId());
                    UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                    String hdPath = ethSignRequest.getDerivationPath();
                    String requestMFP = Hex.toHexString(ethSignRequest.getMasterFingerprint());
                    bundle.putString(REQUEST_ID, uuid.toString());
                    bundle.putString(SIGN_DATA, Hex.toHexString(ethSignRequest.getSignData()));
                    bundle.putString(HD_PATH, "M/" + hdPath);
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
                        byte[] rlpTransaction = ethSignRequest.getSignData();
                        byte[] typedTransaction = EthImpl.decodeTransactionFromRlpValue(rlpTransaction);
                        byte type = typedTransaction[0];
                        switch (type) {
                            case 0x02:
                                mFragment.navigate(R.id.action_to_ethFeeMarketTxConfirmFragment, bundle);
                                break;
                            default:
                                throw new UnknowQrCodeException("unknown transaction!");
                        }
                    }
                } else if (result.getType().equals(ScanResultTypes.UR_BYTES)) {
                    JSONObject object = new JSONObject(new String((byte[]) result.resolve(), StandardCharsets.UTF_8));
                    JSONObject webAuth = object.optJSONObject("data");
                    if (webAuth != null && webAuth.optString("type").equals("webAuth")) {
                        String data = webAuth.getString("data");
                        Bundle bundle = new Bundle();
                        bundle.putString(WEB_AUTH_DATA, data);
                        bundle.putBoolean(IS_SETUP_VAULT, false);
                        mFragment.navigate(R.id.action_QRCodeScan_to_result, bundle);
                    } else {
                        throw new UnExpectedQRException("cannot resolve ur bytes");
                    }
                } else if (result.getType().equals(ScanResultTypes.UOS)) {
                    SubstratePayload sp = new SubstratePayload(result.getData());
                    PolkadotJsTxConfirmViewModel viewModel = ViewModelProviders.of(mActivity)
                            .get(PolkadotJsTxConfirmViewModel.class);
                    Extrinsic extrinsic = sp.extrinsic;
                    if (!viewModel.isNetworkSupported(sp.network)) {
                        throw new UnknownSubstrateChainException("unknown substrate chain");
                    } else if (extrinsic == null ||
                            !viewModel.isTransactionSupported(extrinsic.palletParameter)) {
                        throw new UnsupportedSubstrateTxException("un supported substrate tx");
                    } else if (!viewModel.isAccountMatch(sp.getAccount())) {
                        throw new XfpNotMatchException("Substrate account not match");
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_TX_DATA, result.getData());
                        bundle.putBoolean("substrateTx", true);
                        mFragment.navigate(R.id.action_to_polkadotTxConfirm, bundle);
                    }
                }
            }

            @Override
            public boolean handleException(Exception e) {
                e.printStackTrace();
                if (e instanceof XfpNotMatchException) {
                    mFragment.alert(getString(R.string.account_not_match), getString(R.string.account_not_match_detail));
                    return true;
                } else if (e instanceof UnsupportedSubstrateTxException) {
                    mFragment.alert(getString(R.string.unsupported_polka_tx_type_title),
                            getString(R.string.unsupported_polka_tx_type_content));
                    return true;
                } else if (e instanceof UnknownSubstrateChainException) {
                    mFragment.alert(getString(R.string.unknown_substrate_chain_title),
                            getString(R.string.unknown_substrate_chain_content));
                    return true;
                }
                return false;
            }
        });
        navigate(R.id.action_to_scanner);
    }

    private JSONObject tryDecodeAsJson(String hex) {
        try {
            return new JSONObject(new String(Hex.decode(hex)));
        } catch (Exception ignored) {
        }
        return null;
    }

    private void handleAddAddress() {
        if (fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).exitEditAddressName();
        }
        if (mAddressNumberPicker == null) {
            mAddressNumberPicker = new AddressNumberPicker();
            mAddressNumberPicker.setCallback(this);
        }
        mAddressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.dialog_bottom_sheet, null, false);
        binding.addAddress.setOnClickListener(v -> {
            handleAddAddress();
            dialog.dismiss();

        });
        binding.sync.setOnClickListener(v -> {
            navigate(R.id.action_to_syncFragment);
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void enterSearch() {
        isInSearch = true;
        if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).enterSearch();
        }
        mBinding.searchBar.setVisibility(View.VISIBLE);
        mBinding.search.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(mBinding.search, 0);
        }
    }

    private void exitSearch() {
        isInSearch = false;
        mBinding.search.setText("");
        mBinding.searchBar.setVisibility(View.INVISIBLE);
        mBinding.search.clearFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(mBinding.search.getWindowToken(), 0);
        }
    }

    @Override
    public void onValueSet(int value) {
        AddAddressViewModel viewModel = ViewModelProviders.of(this)
                .get(AddAddressViewModel.class);

        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coinEntity = viewModel.getCoin(coinId);
            if (coinEntity != null) {
                int addrCount = coinEntity.getAddressCount();
                List<String> observableAddressNames = new ArrayList<>();
                for (int i = addrCount; i < value + addrCount; i++) {
                    String name = coinEntity.getDisplayName() + "-" + i;
                    observableAddressNames.add(name);
                }
                viewModel.addAddress(observableAddressNames);

                handler.post(() -> viewModel.getObservableAddState().observe(this, complete -> {
                    if (complete) {
                        handler.postDelayed(dialog::dismiss, 500);
                    }
                }));
            }
        });
    }
}
