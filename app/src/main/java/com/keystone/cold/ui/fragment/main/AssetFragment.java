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

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.keystone.cold.Utilities.IS_SETUP_VAULT;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.keystone.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.keystone.cold.ui.fragment.SyncFragment.DERIVATION_PATH_KEY;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.ETH_NFT;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_CHAIN_ID;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_COLLECTION_NAME;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_CONTRACT_ADDRESS;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_CONTRACT_NAME;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_MEDIA_DATA;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_MINT_ADDRESS;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_NAME;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.KEY_NFT_TYPE;
import static com.keystone.cold.ui.fragment.main.NFTConfirmFragment.SOL_NFT;
import static com.keystone.cold.ui.fragment.main.arweave.ArweaveTxConfirmFragment.KEY_SALT_LEN;
import static com.keystone.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.keystone.cold.ui.fragment.setup.WebAuthResultFragment.WEB_AUTH_DATA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.exception.InvalidETHAccountException;
import com.keystone.coinlib.exception.InvalidNEARAccountException;
import com.keystone.coinlib.exception.InvalidSOLAccountException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.AssetFragmentBinding;
import com.keystone.cold.databinding.DialogBottomSheetBinding;
import com.keystone.cold.db.PresetData;
import com.keystone.cold.db.entity.AddressEntity;
import com.keystone.cold.db.entity.CoinEntity;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.ui.fragment.main.scan.scanner.exceptions.UnExpectedQRException;
import com.keystone.cold.ui.fragment.setup.PreImportFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.ui.modal.ProgressModalDialog;
import com.keystone.cold.ui.views.AuthenticateModal;
import com.keystone.cold.util.AptosTransactionHelper;
import com.keystone.cold.util.SolMessageValidateUtil;
import com.keystone.cold.util.StepFragmentHelper;
import com.keystone.cold.util.SyncAddressUtil;
import com.keystone.cold.util.ViewUtils;
import com.keystone.cold.viewmodel.AddAddressViewModel;
import com.keystone.cold.viewmodel.CoinViewModel;
import com.keystone.cold.viewmodel.PolkadotViewModel;
import com.keystone.cold.viewmodel.PublicKeyViewModel;
import com.keystone.cold.viewmodel.SetupVaultViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.keystone.cold.viewmodel.exceptions.UnknownSubstrateChainException;
import com.keystone.cold.viewmodel.exceptions.UnsupportedSubstrateTxException;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;
import com.keystone.cold.viewmodel.tx.psbt.PSBTViewModel;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;
import com.sparrowwallet.hummingbird.registry.EthNFTItem;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignRequest;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignRequest;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignRequest;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignRequest;
import com.sparrowwallet.hummingbird.registry.near.NearSignRequest;
import com.sparrowwallet.hummingbird.registry.solana.SolNFTItem;
import com.sparrowwallet.hummingbird.registry.solana.SolSignRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AssetFragment extends BaseFragment<AssetFragmentBinding>
        implements Toolbar.OnMenuItemClickListener, NumberPickerCallback {

    public static final String TAG = "AssetFragment";

    public static final int DIALOG_DISMISS_DELAY_TIME = 500;

    private final ObservableField<String> query = new ObservableField<>();

    private boolean isInSearch;
    private Fragment[] fragments;
    private boolean showPublicKey;
    private String coinId;
    private String coinCode;
    private AddressNumberPicker mAddressNumberPicker;
    private boolean hasAddress;
    private WatchWallet watchWallet;
    private DataRepository mRepository;

    @Override
    protected int setView() {
        return R.layout.asset_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mRepository = ((MainApplication) mActivity.getApplication()).getRepository();
        if (watchWallet == WatchWallet.CORE_WALLET) {
            Bundle data = requireArguments();
            coinId = data.getString(KEY_COIN_ID);
            coinCode = data.getString(KEY_COIN_CODE);
            mBinding.customTitle.setVisibility(View.GONE);
            if (coinCode.equals(Coins.BTC_CORE_WALLET.coinCode())) {
                mBinding.toolbar.setTitle(Coins.BTC_CORE_WALLET.coinName());
            } else {
                // We treat ETH as AVAX, for they share the same accounts;
                mBinding.toolbar.setTitle(Coins.AVAX.coinName());
            }
        }
        if (watchWallet == WatchWallet.BIT_KEEP) {
            Bundle data = requireArguments();
            coinId = data.getString(KEY_COIN_ID);
            coinCode = data.getString(KEY_COIN_CODE);
            mBinding.customTitle.setVisibility(View.GONE);
            if (coinCode.equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) {
                mBinding.toolbar.setTitle("Bitcoin");
            } else {
                mBinding.toolbar.setTitle(Coins.ETH.coinName());
            }
        }
        if (watchWallet == WatchWallet.METAMASK) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity).replace("MetaMask", "MM"));
            mBinding.customTitle.setVisibility(View.GONE);
            mBinding.account.setText(ETHAccount.ofCode(Utilities.getCurrentEthAccount(mActivity)).getName());
            mBinding.account.setVisibility(View.VISIBLE);
            coinId = Coins.ETH.coinId();
            coinCode = Coins.ETH.coinCode();
        } else if (watchWallet == WatchWallet.ARConnect) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.AR.coinId();
            coinCode = Coins.AR.coinCode();
        } else if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.XRP.coinId();
            coinCode = Coins.XRP.coinCode();
        } else if (watchWallet == WatchWallet.SOLANA) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.SOL.coinId();
            coinCode = Coins.SOL.coinCode();
        } else if (watchWallet == WatchWallet.NEAR) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.NEAR.coinId();
            coinCode = Coins.NEAR.coinCode();
        } else if (watchWallet == WatchWallet.APTOS) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            mBinding.customTitle.setVisibility(View.GONE);
            coinId = Coins.APTOS.coinId();
            coinCode = Coins.APTOS.coinCode();
        } else {
            Bundle data = requireArguments();
            coinId = data.getString(KEY_COIN_ID);
            coinCode = data.getString(KEY_COIN_CODE);
            if (watchWallet == WatchWallet.KEPLR_WALLET) {
                mBinding.toolbar.setTitle(Coins.coinNameOfCoinId(coinId));
                mBinding.customTitle.setVisibility(View.GONE);
            } else {
                showPublicKey = Coins.showPublicKey(coinCode);
            }
        }
        updateUI();
    }

    private void updateUI() {
//        if (watchWallet == WatchWallet.POLKADOT_JS) {
//            mBinding.button.setVisibility(View.VISIBLE);
//            mBinding.button.setOnClickListener(v -> syncPolkadot());
//        } else {
        mBinding.toolbar.inflateMenu(getMenuResId());
        mBinding.button.setVisibility(View.GONE);
        MenuItem menuItem = mBinding.toolbar.getMenu().findItem(R.id.action_more);
        if (judgeShowBadge()) {
            showBadge(menuItem);
        }
//        }
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (watchWallet == WatchWallet.XRP_TOOLKIT || watchWallet == WatchWallet.METAMASK ||
                    watchWallet == WatchWallet.SOLANA || watchWallet == WatchWallet.NEAR ||
                    watchWallet == WatchWallet.APTOS || watchWallet == WatchWallet.ARConnect) {
                ((MainActivity) mActivity).toggleDrawer(v);
            } else {
                navigateUp();
            }
        });
        initSearchView();
        initTabs();
    }

    private int getMenuResId() {
        switch (watchWallet) {
            case XRP_TOOLKIT:
                return R.menu.xrp_toolkit;
            case METAMASK:
            case SOLANA:
            case NEAR:
            case APTOS:
            case POLKADOT_JS:
            case CORE_WALLET:
            case BIT_KEEP:
            case KEPLR_WALLET:
            case ARConnect:
                return R.menu.metamask;
            case KEYSTONE:
                if (coinCode.equals(Coins.DOT.coinCode()) || coinCode.equals(Coins.KSM.coinCode()))
                    return R.menu.asset_without_add;
                if (Coins.isUTXOLike(coinCode)) {
                    return R.menu.more;
                }
                return R.menu.asset;
            default:
                return (showPublicKey) ? R.menu.asset_without_add : R.menu.asset;
        }
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
        String tabOne = showPublicKey && !hasAddress ? getString(R.string.tab_my_pubkey)
                : getString(R.string.tab_my_address);
        if (watchWallet == WatchWallet.METAMASK) {
            tabOne = getString(R.string.tab_my_account);
        } else if (watchWallet == WatchWallet.NEAR) {
            tabOne = getString(R.string.tab_public_key);
        }
        String tabTwo = getString(R.string.tab_transaction_history);
        String[] title = {tabOne, tabTwo};
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
        checkAndAddNewCoins();
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(), coinId);
        CoinViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(CoinViewModel.class);

        mBinding.setCoinViewModel(viewModel);
        subscribeUi(viewModel);

        if (watchWallet == WatchWallet.SOLANA) {
            viewModel.preGenerateSolDerivationAddress();
        } else if (watchWallet == WatchWallet.NEAR) {
            viewModel.preGenerateNearDerivationAddress();
        }

        if (coinCode.equals(Coins.AR.coinCode())) {
            ArweaveViewModel arweaveViewModel = ViewModelProviders.of(this).get(ArweaveViewModel.class);
            LiveData<Boolean> hasAR = arweaveViewModel.hasArweaveAddress();
            hasAR.observe(this, value -> {
                if (!value) {
                    navigate(R.id.action_to_ArweaveAuthFragment);
                }
                hasAR.removeObservers(this);
            });
        }
    }


    private void checkAndAddNewCoins() {
        SetupVaultViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(SetupVaultViewModel.class);
        AppExecutors.getInstance().diskIO().execute(()
                -> viewModel.presetData(PresetData.generateCoins(mActivity), null)
        );

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
                if (watchWallet == WatchWallet.METAMASK
                        || watchWallet == WatchWallet.SOLANA || watchWallet == WatchWallet.NEAR
                        || watchWallet == WatchWallet.APTOS || watchWallet == WatchWallet.CORE_WALLET
                        || watchWallet == WatchWallet.BIT_KEEP || watchWallet == WatchWallet.ARConnect
                        || watchWallet == WatchWallet.KEPLR_WALLET) {
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

    private void showBadge(MenuItem menuItem) {
        Drawable menu = Objects.requireNonNull(menuItem).getIcon();
        int badgeSize = (int) getResources().getDimension(R.dimen.default_badge_size);
        Drawable menuWithBadge = ViewUtils.addBadge(getResources(), menu, badgeSize);
        menuItem.setIcon(menuWithBadge);
    }

    private void hideBadge() {
        MenuItem menuItem = mBinding.toolbar.getMenu().findItem(R.id.action_more);
        menuItem.setIcon(R.drawable.more);
    }

    public static final String SIGN_DATA = "signData";
    public static final String REQUEST_ID = "requestId";
    public static final String HD_PATH = "hdPath";
    public static final String DATA_TYPE = "dataType";
    public static final String ORIGIN_DATA = "originData";
    public static final String CUSTOM_CHAIN_IDENTIFIER = "customChainIdentifier";

    private void scanQrCode() {
        ScannerState scannerState = new ScannerState() {
            @Override
            public void handleScanResult(ScanResult result) throws Exception {
                StepFragmentHelper.getInstance().setStartingPoint(AssetFragment.class.getName());
                if (result.getType().equals(ScanResultTypes.PLAIN_TEXT)) {
                    throw new UnknowQrCodeException("unknown transaction!");
                } else if (result.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
                    handleETHSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_BYTES)) {
                    handleURBytes(result);
                } else if (result.getType().equals(ScanResultTypes.UR_ETH_NFT_ITEM)) {
                    handleETHNFTItem(result);
                } else if (result.getType().equals(ScanResultTypes.UR_SOL_SIGN_REQUEST)) {
                    handleSolSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_SOL_NFT_ITEM)) {
                    handleSolNFTItem(result);
                } else if (result.getType().equals(ScanResultTypes.UR_NEAR_SIGN_REQUEST)) {
                    handleNearSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_APTOS_SIGN_REQUEST)) {
                    handleAptosSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_CRYPTO_PSBT)) {
                    handleCryptoPSBT(result);
                } else if (result.getType().equals(ScanResultTypes.UR_COSMOS_SIGN_REQUEST)) {
                    handleCosmosSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_ARWEAVE_SIGN_REQUEST)) {
                    handleArweaveSignRequest(result);
                } else if (result.getType().equals(ScanResultTypes.UR_EVM_SIGN_REQUEST)) {
                    handleEvmSignRequest(result);
                } else {
                    throw new UnknowQrCodeException("unknown transaction!");
                }
            }

            private void handleURBytes(ScanResult result) throws JSONException, UnExpectedQRException {
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
            }

            private void handleETHNFTItem(ScanResult result) {
                EthNFTItem ethnftItem = (EthNFTItem) result.resolve();
                String name = ethnftItem.getName();
                int chainId = ethnftItem.getChainId();
                String contractAddress = ethnftItem.getContractAddress();
                String contractName = ethnftItem.getContractName();
                String mediaData = ethnftItem.getMediaData();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_NFT_TYPE, ETH_NFT);
                bundle.putLong(KEY_CHAIN_ID, chainId);
                bundle.putString(KEY_CONTRACT_ADDRESS, contractAddress);
                bundle.putString(KEY_CONTRACT_NAME, contractName);
                bundle.putString(KEY_NAME, name);
                bundle.putString(KEY_MEDIA_DATA, mediaData);
                mFragment.navigate(R.id.action_QRCodeScan_to_nftConfirmFragment, bundle);
            }

            private void handleETHSignRequest(ScanResult result) throws UnknowQrCodeException, XfpNotMatchException, InvalidETHAccountException, InvalidTransactionException {
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
                    throw new InvalidTransactionException("unknown hd path");
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

            private void handleAptosSignRequest(ScanResult result) throws XfpNotMatchException, UnknowQrCodeException {
                AptosSignRequest aptosSignRequest = (AptosSignRequest) result.resolve();
                String requestMFP = Hex.toHexString(aptosSignRequest.getMasterFingerprint());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                ByteBuffer uuidBuffer = ByteBuffer.wrap(aptosSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = aptosSignRequest.getDerivationPath();
                String signData = Hex.toHexString(aptosSignRequest.getSignData());
                Bundle bundle = new Bundle();
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putString(SIGN_DATA, signData);
                bundle.putString(HD_PATH, "M/" + hdPath);
                bundle.putString(ORIGIN_DATA, aptosSignRequest.getOrigin());
                AptosSignRequest.DataType dataType = aptosSignRequest.getType();
                switch (dataType) {
                    case MESSAGE:
                        mFragment.navigate(R.id.action_to_aptosMessageFragment, bundle);
                        break;
                    case SINGLE:
                        AptosTransactionHelper.Type type = AptosTransactionHelper.judgeDataType(signData);
                        if (type == AptosTransactionHelper.Type.MESSAGE) {
                            mFragment.navigate(R.id.action_to_aptosMessageFragment, bundle);
                        } else {
                            mFragment.navigate(R.id.action_to_aptosTxConfirmFragment, bundle);
                        }
                        break;
                    case MULTI:
                        throw new UnknowQrCodeException("unknown transaction!");
                }
            }

            private void handleNearSignRequest(ScanResult result) throws InvalidTransactionException, InvalidNEARAccountException, XfpNotMatchException {
                NearSignRequest nearSignRequest = (NearSignRequest) result.resolve();
                ByteBuffer uuidBuffer = ByteBuffer.wrap(nearSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = nearSignRequest.getDerivationPath();

                NEARAccount current = NEARAccount.ofCode(Utilities.getCurrentNearAccount(mFragment.getActivity()));
                NEARAccount target = NEARAccount.getAccountByPath(hdPath);
                if (target == null) {
                    throw new InvalidTransactionException("unknown hd path");
                }
                if (!target.equals(current)) {
                    if (!current.isChildrenPath(hdPath)) {
                        throw new InvalidNEARAccountException("not expected NEAR account", current, target);
                    }
                }
                String requestMFP = Hex.toHexString(nearSignRequest.getMasterFingerprint());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }

                List<byte[]> signDataList = nearSignRequest.getSignDataList();
                List<String> signHexList = new ArrayList<>(signDataList.size());
                for (byte[] signData : signDataList) {
                    signHexList.add(Hex.toHexString(signData));
                }
                Bundle bundle = new Bundle();
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putSerializable(SIGN_DATA, (Serializable) signHexList);
                bundle.putString(HD_PATH, "M/" + hdPath);
                mFragment.navigate(R.id.action_to_nearTxConfirmFragment, bundle);
            }

            private void handleSolNFTItem(ScanResult result) {
                SolNFTItem solNFTItem = (SolNFTItem) result.resolve();
                String mintAddress = solNFTItem.getMintAddress();
                String collectionName = solNFTItem.getCollectionName();
                String name = solNFTItem.getName();
                String mediaData = solNFTItem.getMediaData();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_NFT_TYPE, SOL_NFT);
                bundle.putString(KEY_MINT_ADDRESS, mintAddress);
                bundle.putString(KEY_COLLECTION_NAME, collectionName);
                bundle.putString(KEY_NAME, name);
                bundle.putString(KEY_MEDIA_DATA, mediaData);
                mFragment.navigate(R.id.action_QRCodeScan_to_nftConfirmFragment, bundle);
            }

            private void handleSolSignRequest(ScanResult result) throws InvalidTransactionException, InvalidSOLAccountException, XfpNotMatchException {
                SolSignRequest solSignRequest = (SolSignRequest) result.resolve();
                Bundle bundle = new Bundle();
                ByteBuffer uuidBuffer = ByteBuffer.wrap(solSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String hdPath = solSignRequest.getDerivationPath();
                SOLAccount current = SOLAccount.ofCode(Utilities.getCurrentSolAccount(mFragment.getActivity()));
                SOLAccount target = SOLAccount.getAccountByPath(hdPath);
                if (target == null) {
                    throw new InvalidTransactionException("unknown hd path");
                }
                if (!target.equals(current)) {
                    if (!current.isChildrenPath(hdPath)) {
                        throw new InvalidSOLAccountException("not expected SOL account", current, target);
                    }
                }
                String requestMFP = Hex.toHexString(solSignRequest.getMasterFingerprint());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                bundle.putString(REQUEST_ID, uuid.toString());
                String signData = Hex.toHexString(solSignRequest.getSignData());
                bundle.putString(SIGN_DATA, signData);
                bundle.putString(HD_PATH, "M/" + hdPath);
                SolMessageValidateUtil.DataType dataType = SolMessageValidateUtil.judgeDataType(signData);
                switch (dataType) {
                    case TRANSACTION:
                        mFragment.navigate(R.id.action_to_solTxConfirmFragment, bundle);
                        break;
                    case MESSAGE:
                        mFragment.navigate(R.id.action_to_solSignMessageFragment, bundle);
                        break;
                    case INVALIDATE:
                        throw new InvalidTransactionException("Invalid sign data");
                }
            }

            private void handleArweaveSignRequest(ScanResult result) throws InvalidTransactionException, XfpNotMatchException {
                ArweaveSignRequest arweaveSignRequest = (ArweaveSignRequest) result.resolve();
                Bundle bundle = new Bundle();
                ByteBuffer uuidBuffer = ByteBuffer.wrap(arweaveSignRequest.getRequestId());
                UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
                String requestMFP = Hex.toHexString(arweaveSignRequest.getMasterFingerprint());
                String MFP = new GetMasterFingerprintCallable().call();
                if (!requestMFP.equalsIgnoreCase(MFP)) {
                    throw new XfpNotMatchException("Master fingerprint not match");
                }
                bundle.putString(REQUEST_ID, uuid.toString());
                bundle.putInt(KEY_SALT_LEN, arweaveSignRequest.getSaltLen().getLength());
                String signData = Hex.toHexString(arweaveSignRequest.getSignData());
                bundle.putString(SIGN_DATA, signData);
                ArweaveSignRequest.SignType signType = arweaveSignRequest.getSignType();

                switch (signType) {
                    case TRANSACTION:
                        mFragment.navigate(R.id.action_to_arweaveTxConfirmFragment, bundle);
                        break;
                    case DATAITEM:
                        throw new InvalidTransactionException("Transaction type DataItem not supported yet");
                    case MESSAGE:
                        mFragment.navigate(R.id.action_to_arweaveSignMessageFragment, bundle);
                        break;
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
                } else if (e instanceof JSONException) {
                    mFragment.alert(getString(R.string.unresolve_tx), getString(R.string.unresolve_tx_hint, watchWallet.getWalletName(mActivity)));
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
                } else if (e instanceof InvalidTransactionException) {
                    mFragment.alert(getString(R.string.invalid_data), getString(R.string.incorrect_tx_data));
                    return true;
                } else if (e instanceof InvalidSOLAccountException) {
                    mFragment.alertDoubleButtonModal(getString(R.string.invalid_data),
                            getString(R.string.invalid_account_tx, ((InvalidSOLAccountException) e).getAccount().getName(), ((InvalidSOLAccountException) e).getTarget().getName(), ((InvalidSOLAccountException) e).getTarget().getName()),
                            getString(R.string.cancel),
                            getString(R.string.switch_wallet),
                            null, () -> {
                                Utilities.setCurrentSolAccount(mActivity, ((InvalidSOLAccountException) e).getTarget().getCode());
                                popBackStack(R.id.assetFragment, false);
                            });
                    return true;
                } else if (e instanceof InvalidNEARAccountException) {
                    mFragment.alertDoubleButtonModal(getString(R.string.invalid_data),
                            getString(R.string.invalid_account_tx, ((InvalidNEARAccountException) e).getAccount().getName(), ((InvalidNEARAccountException) e).getTarget().getName(), ((InvalidNEARAccountException) e).getTarget().getName()),
                            getString(R.string.cancel),
                            getString(R.string.switch_wallet),
                            null, () -> {
                                Utilities.setCurrentNearAccount(mActivity, ((InvalidNEARAccountException) e).getTarget().getCode());
                                popBackStack(R.id.assetFragment, false);
                            });
                    return true;
                }
                return false;
            }
        };
        List<ScanResultTypes> desiredResults = new ArrayList<>(Arrays.asList(ScanResultTypes.PLAIN_TEXT, ScanResultTypes.UR_BYTES));
        if (watchWallet == WatchWallet.METAMASK) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_ETH_NFT_ITEM));
        } else if (watchWallet == WatchWallet.SOLANA) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_SOL_SIGN_REQUEST, ScanResultTypes.UR_SOL_NFT_ITEM));
        } else if (watchWallet == WatchWallet.NEAR) {
            desiredResults.addAll(Collections.singletonList(ScanResultTypes.UR_NEAR_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.APTOS) {
            desiredResults.addAll(Collections.singletonList(ScanResultTypes.UR_APTOS_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.CORE_WALLET) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_CRYPTO_PSBT));
        } else if (watchWallet == WatchWallet.BIT_KEEP) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_CRYPTO_PSBT));
        } else if (watchWallet == WatchWallet.KEPLR_WALLET) {
            desiredResults.addAll(Arrays.asList(ScanResultTypes.UR_COSMOS_SIGN_REQUEST, ScanResultTypes.UR_ETH_SIGN_REQUEST, ScanResultTypes.UR_EVM_SIGN_REQUEST));
        } else if (watchWallet == WatchWallet.ARConnect) {
            desiredResults.addAll(Collections.singletonList(ScanResultTypes.UR_ARWEAVE_SIGN_REQUEST));
        }
        scannerState.setDesiredResults(desiredResults);
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(scannerState);
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
        Runnable closeDialog = () -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        };
        addClickAccountProcess(binding.addAddress, closeDialog);
        addClickSyncProcess(binding.sync, binding.syncText, closeDialog);
        addCLickChangePathProcess(binding.changePath, closeDialog);
        addClickTutorialsProcess(binding.tutorials, closeDialog);
        addClickResetDBProcess(binding.resetDb, closeDialog);
        addExportXPubProcess(binding.exportXpub, closeDialog);
        addSearchProcess(binding.searchAddress, closeDialog);
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void addClickTutorialsProcess(View view, Runnable additionProcess) {
        if (isKeystoneExportXPub()) {
            view.setVisibility(View.GONE);
        } else {
            view.setOnClickListener(v -> {
                navigate(R.id.action_to_tutorialsFragment);
                additionProcess.run();
            });
        }
    }

    private boolean isKeystoneExportXPub() {
        return watchWallet.equals(WatchWallet.KEYSTONE) && Coins.isUTXOLike(coinCode);
    }

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_to_preImportFragment, bundle);
    };


    private void addExportXPubProcess(View view, Runnable additionProcess) {
        if (!isKeystoneExportXPub()) {
            view.setVisibility(View.GONE);
        } else {
            view.setOnClickListener(v -> {
                AuthenticateModal.show(mActivity,
                        getString(R.string.password_modal_title),
                        "",
                        false,
                        (token) -> {
                            Bundle data = new Bundle();
                            data.putString(KEY_COIN_CODE, coinCode);
                            navigate(R.id.action_to_exportXPubFragment, data);
                        }, forgetPassword);
                additionProcess.run();
            });
        }
    }

    private void addSearchProcess(View view, Runnable additionProcess) {
        if (!isKeystoneExportXPub()) {
            view.setVisibility(View.GONE);
        } else {
            view.setOnClickListener(v -> {
                enterSearch();
                additionProcess.run();
            });
        }
    }

    private void addClickResetDBProcess(View view, Runnable additionProcess) {
        if (!WatchWallet.getWatchWallet(getContext()).equals(WatchWallet.POLKADOT_JS)) {
            view.setVisibility(View.GONE);
        } else {
            view.setOnClickListener(v -> {
                ModalDialog.showTwoButtonCommonModal(mActivity, getString(R.string.warning), getString(R.string.reset_polkadot_db_hint), getString(R.string.cancel), getString(R.string.confirm), additionProcess, () -> {
                    PolkadotViewModel viewModel = ViewModelProviders.of(mActivity).get(PolkadotViewModel.class);
                    viewModel.resetDB();
                });
                additionProcess.run();
            });
        }
    }

    private void addCLickChangePathProcess(View view, Runnable additionProcess) {
        if (isHideChangePath()) {
            view.setVisibility(View.GONE);
        }
        view.setOnClickListener(v -> {
            if (watchWallet == WatchWallet.METAMASK || watchWallet == WatchWallet.SOLANA || watchWallet == WatchWallet.NEAR || watchWallet == WatchWallet.CORE_WALLET || watchWallet == WatchWallet.BIT_KEEP) {
                navigate(R.id.action_assetFragment_to_changeDerivePathFragment);
            }
            additionProcess.run();
        });
    }

    private void addClickSyncProcess(View view, View anchor, Runnable additionProcess) {
        if (isKeystoneExportXPub()) {
            view.setVisibility(View.GONE);
        } else {
            boolean isShowBadge = judgeShowBadge();
            BadgeView bgView = null;
            if (isShowBadge) {
                bgView = generateBadgeView(anchor);
            }
            setSyncViewListener(view, additionProcess, bgView);
        }
    }

    private boolean judgeShowBadge() {
        boolean isShowBadge = false;
        if (watchWallet == WatchWallet.METAMASK && !Utilities.hasUserClickEthSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.SOLANA && !Utilities.hasUserClickSolSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.NEAR && !Utilities.hasUserClickNearSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.POLKADOT_JS && coinCode.equals(Coins.DOT.coinCode()) && !Utilities.hasUserClickPolkadotSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.POLKADOT_JS && coinCode.equals(Coins.KSM.coinCode()) && !Utilities.hasUserClickKusamaSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.APTOS && !Utilities.hasUserClickAptosSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.CORE_WALLET && !Utilities.hasUserClickCoreWalletSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.KEPLR_WALLET && !Utilities.hasUserClickKeplrSyncLock(mActivity)) {
            isShowBadge = true;
        } else if (watchWallet == WatchWallet.ARConnect && !Utilities.hasUserClickArweaveSyncLock(mActivity)) {
            isShowBadge = true;
        }
        return isShowBadge;
    }

    private void setUserClickSyncLock() {
        switch (watchWallet) {
            case METAMASK:
                Utilities.setUserClickEthSyncLock(mActivity);
                break;
            case SOLANA:
                Utilities.setUserClickSolSyncLock(mActivity);
                break;
            case NEAR:
                Utilities.setUserClickNearSyncLock(mActivity);
                break;
            case APTOS:
                Utilities.setUserClickAptosSyncLock(mActivity);
                break;
            case POLKADOT_JS:
                if (coinCode.equals(Coins.DOT.coinCode())) {
                    Utilities.setUserClickPolkadotSyncLock(mActivity);
                } else {
                    Utilities.setUserClickKusamaSyncLock(mActivity);
                }
                break;
            case CORE_WALLET:
                Utilities.setUserClickCoreWalletSyncLock(mActivity);
                break;
            case KEPLR_WALLET:
                Utilities.setUserClickKeplrSyncLock(mActivity);
                break;
            case ARConnect:
                Utilities.setUserClickArweaveSyncLock(mActivity);
            default:
                break;
        }
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

    private void setSyncViewListener(View view, Runnable additionProcess, BadgeView finalBgView) {
        view.setOnClickListener(v -> {
            if (watchWallet == WatchWallet.METAMASK || watchWallet == WatchWallet.CORE_WALLET || watchWallet.equals(WatchWallet.BIT_KEEP) || watchWallet == WatchWallet.KEPLR_WALLET || watchWallet == WatchWallet.ARConnect) {
                navigate(R.id.action_to_syncFragment);
            } else if (watchWallet == WatchWallet.SOLANA) {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_COIN_ID, coinId);
                navigate(R.id.action_assetFragment_to_addressSyncAddress, bundle);
            } else if (watchWallet == WatchWallet.NEAR) {
                syncNearAddress();
            } else if (watchWallet == WatchWallet.APTOS) {
                syncAptosAddress();
            } else if (watchWallet == WatchWallet.POLKADOT_JS) {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_COIN_ID, coinId);
                navigate(R.id.action_assetFragment_to_addressSyncAddress, bundle);
            }
            if (judgeShowBadge()) {
                setUserClickSyncLock();
            }
            if (finalBgView != null) {
                finalBgView.unbind();
                hideBadge();
            }
            additionProcess.run();
        });
    }

    private void syncAptosAddress() {
        SyncAddressUtil.Callback callback = new SyncAddressUtil.Callback() {
            @Override
            public void oneAddress(SyncInfo syncInfo) {
                List<SyncInfo> syncInfoList = Collections.singletonList(syncInfo);
                Bundle bundle = new Bundle();
                bundle.putSerializable(DERIVATION_PATH_KEY, (Serializable) syncInfoList);
                navigate(R.id.action_to_syncFragment, bundle);
            }

            @Override
            public void moreThanOneAddress() {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_COIN_ID, coinId);
                navigate(R.id.action_assetFragment_to_addressSyncAddress, bundle);
            }

            @Override
            public void noAddress() {
                Toast.makeText(mActivity, R.string.get_address_fail_info, Toast.LENGTH_SHORT).show();
            }
        };
        SyncAddressUtil.getSyncAddressInfo(coinId, null, watchWallet, callback);
    }

    private void syncNearAddress() {
        String code = Utilities.getCurrentNearAccount(mActivity);
        if (NEARAccount.ofCode(code) == NEARAccount.MNEMONIC) {
            SyncAddressUtil.Callback callback = new SyncAddressUtil.Callback() {
                @Override
                public void oneAddress(SyncInfo syncInfo) {
                    List<SyncInfo> syncInfoList = Collections.singletonList(syncInfo);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(DERIVATION_PATH_KEY, (Serializable) syncInfoList);
                    navigate(R.id.action_to_syncFragment, bundle);
                }

                @Override
                public void moreThanOneAddress() {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_COIN_ID, coinId);
                    navigate(R.id.action_assetFragment_to_addressSyncAddress, bundle);
                }

                @Override
                public void noAddress() {
                    Toast.makeText(mActivity, R.string.get_address_fail_info, Toast.LENGTH_SHORT).show();
                }
            };
            SyncAddressUtil.getSyncAddressInfo(coinId, code, watchWallet, callback);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_COIN_ID, coinId);
            navigate(R.id.action_assetFragment_to_addressSyncAddress, bundle);
        }
    }

    private boolean isHideChangePath() {
        if (isKeystoneExportXPub()) {
            return true;
        }
        if (watchWallet == WatchWallet.APTOS || watchWallet == WatchWallet.ARConnect
                || watchWallet == WatchWallet.POLKADOT_JS || watchWallet == WatchWallet.KEPLR_WALLET) {
            return true;
        }
        if ((watchWallet == WatchWallet.CORE_WALLET || watchWallet.equals(WatchWallet.BIT_KEEP)) && coinCode.equals(Coins.BTC_NATIVE_SEGWIT.coinCode())) {
            return true;
        }
        return false;
    }

    private boolean isNearMnemonic() {
        if (watchWallet == WatchWallet.NEAR) {
            String code = Utilities.getCurrentNearAccount(mActivity);
            return NEARAccount.ofCode(code) == NEARAccount.MNEMONIC;
        }
        return false;
    }

    private void addClickAccountProcess(View view, Runnable additionProcess) {
        if (hideAddAddress()) {
            view.setVisibility(View.GONE);
        }
        view.setOnClickListener(v -> {
            handleAddAddress();
            additionProcess.run();
        });
    }

    private boolean hideAddAddress() {
        boolean hide = false;
        if (watchWallet == WatchWallet.SOLANA && SOLAccount.ofCode(Utilities.getCurrentSolAccount(mActivity)) == SOLAccount.SOLFLARE_BIP44_ROOT) {
            hide = true;
        } else if (watchWallet == WatchWallet.NEAR && NEARAccount.ofCode(Utilities.getCurrentNearAccount(mActivity)) == NEARAccount.MNEMONIC) {
            hide = true;
        } else if (watchWallet == WatchWallet.KEPLR_WALLET || watchWallet == WatchWallet.ARConnect) {
            hide = true;
        }
        return hide;
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
        if (watchWallet == WatchWallet.METAMASK) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                CoinEntity coinEntity = viewModel.getCoin(coinId);
                viewModel.addEthAccountAddress(value, coinEntity, () -> handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME));
            });
        } else if (watchWallet == WatchWallet.SOLANA) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                CoinEntity coinEntity = viewModel.getCoin(coinId);
                viewModel.addSolAccountAddress(value, coinEntity, () -> {
                    if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
                        ((AddressFragment) fragments[0]).updateAddressList();
                    }
                    handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME);
                });
            });
        } else if (watchWallet == WatchWallet.NEAR) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                CoinEntity coinEntity = viewModel.getCoin(coinId);
                viewModel.addNearAccountAddress(value, coinEntity, () -> {
                    if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
                        ((AddressFragment) fragments[0]).updateAddressList();
                    }
                    handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME);
                });
            });
        } else if (watchWallet == WatchWallet.APTOS) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                CoinEntity coinEntity = viewModel.getCoin(coinId);
                viewModel.addAptosAddress(value, coinEntity, () -> {
                    if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
                        ((AddressFragment) fragments[0]).updateAddressList();
                    }
                    handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME);
                });
            });
        } else if (watchWallet == WatchWallet.POLKADOT_JS) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                CoinEntity coinEntity = viewModel.getCoin(coinId);
                viewModel.addPolkadotAddress(value, coinEntity, () -> {
//                    if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
//                        ((AddressFragment) fragments[0]).updateAddressList();
//                    }
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
                                dot.removeObservers(this);
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
                                ksm.removeObservers(this);
                            }));
                        } catch (PolkadotViewModel.PolkadotException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME);
                });
            });
        } else {
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
                            handler.postDelayed(dialog::dismiss, DIALOG_DISMISS_DELAY_TIME);
                        }
                    }));
                }
            });
        }
    }
}
