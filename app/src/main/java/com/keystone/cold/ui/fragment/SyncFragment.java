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

package com.keystone.cold.ui.fragment;

import static com.keystone.cold.ui.fragment.setup.SyncWatchWalletGuide.getSyncWatchWalletGuide;
import static com.keystone.cold.ui.fragment.setup.SyncWatchWalletGuide.getSyncWatchWalletGuideTitle;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.SyncFragmentBinding;
import com.keystone.cold.integration.chains.ArweaveViewModel;
import com.keystone.cold.integration.corewallet.CoreWalletViewModel;
import com.keystone.cold.integration.cosmoswallet.KeplrWalletViewModel;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.main.SyncInfo;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.SyncViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.UR;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SyncFragment extends SetupVaultBaseFragment<SyncFragmentBinding> {

    public static final String DERIVATION_PATH_KEY = "derivation_paths_key";

    public static final String TAG = "SyncFragment";
    private SyncViewModel syncViewModel;
    private WatchWallet watchWallet;
    private String coinCode;
    private boolean fromSyncGuide = false;
    private boolean isRefreshing = false;

    private MutableLiveData<UR> URLiveData;

    private List<SyncInfo> syncInfoList;

    @Override
    protected int setView() {
        return R.layout.sync_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        Bundle data = getArguments();
        if (data != null) {
            coinCode = data.getString("coinCode");
            fromSyncGuide = getArguments().getBoolean("fromSyncGuide");

            syncInfoList = (List<SyncInfo>) getArguments().getSerializable(DERIVATION_PATH_KEY);

        }
        if (!fromSyncGuide) {
            mBinding.complete.setVisibility(View.GONE);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.setTitle("");
        mBinding.complete.setOnClickListener(v -> {
            if (mActivity instanceof MainActivity) {
                if (watchWallet == WatchWallet.METAMASK) {
                    Utilities.setWeb3GuideTimes(mActivity, Utilities.getWeb3GuideTimes(mActivity) + 1);
                    startActivity(new Intent(mActivity, MainActivity.class));
                    mActivity.finish();
                } else {
                    startActivity(new Intent(mActivity, MainActivity.class));
                    mActivity.finish();
                }
            } else {
                if (watchWallet == WatchWallet.POLKADOT_JS && coinCode.equals(Coins.DOT.coinCode())) {
                    Bundle bundle = getArguments();
                    bundle.putString("coinCode", Coins.KSM.coinCode());
                    navigate(R.id.action_to_syncWatchWalletGuide, bundle);
                } else {
                    startActivity(new Intent(mActivity, MainActivity.class));
                    mActivity.finish();
                }
            }
        });
        setupUIWithWatchWallet();
        syncViewModel = ViewModelProviders.of(mActivity).get(SyncViewModel.class);
        generateSyncData();
    }

    private void setupUIWithWatchWallet() {
        mBinding.info.setOnClickListener(v -> {
            switch (watchWallet) {
                case METAMASK:
                case SOLANA:
                case XRP_TOOLKIT:
                case POLKADOT_JS:
                case NEAR:
                case APTOS:
                case CORE_WALLET:
                case BIT_KEEP:
                case KEPLR_WALLET:
                case ARConnect:
                    navigate(R.id.action_to_tutorialsFragment);
                    break;
                default:
                    showHint();
            }
        });
        switch (watchWallet) {
            case KEYSTONE:
                mBinding.hint.setText(R.string.sync_with_keystone_vault);
                break;
            case XRP_TOOLKIT:
                mBinding.hint.setText(R.string.sync_with_xrp_toolkit);
                mBinding.address.setVisibility(View.VISIBLE);
                break;
            case POLKADOT_JS:
            case METAMASK:
            case SOLANA:
            case NEAR:
            case APTOS:
            case CORE_WALLET:
            case BIT_KEEP:
            case KEPLR_WALLET:
            case ARConnect:
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 9, 0, 0);
                mBinding.content.setLayoutParams(params);
                mBinding.complete.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showHint() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(getString(getSyncWatchWalletGuideTitle(watchWallet), coinCode));
        binding.subTitleHint.setVisibility(View.GONE);
        binding.subTitle.setText(getString(getSyncWatchWalletGuide(watchWallet),
                Coins.coinNameFromCoinCode(coinCode), coinCode));
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void generateSyncData() {
        switch (watchWallet) {
            case KEYSTONE:
                syncViewModel.generateSyncKeystone().observe(this, sync -> {
                    if (!TextUtils.isEmpty(sync)) {
                        mBinding.dynamicQrcodeLayout.qrcode.setData(sync);
                    }
                });
                break;
            case XRP_TOOLKIT:
                int index = getArguments().getInt("index");
                syncViewModel.generateSyncXumm(index).observe(this, xrpSyncData -> {
                    if (xrpSyncData != null) {
                        mBinding.dynamicQrcodeLayout.qrcode.setData(generateXrpToolsSyncData(xrpSyncData));
                        mBinding.addressName.setText(xrpSyncData.addressEntity.getName());
                        mBinding.addressInfo.setText(String.format("%s\n(%s)",
                                xrpSyncData.addressEntity.getAddressString(),
                                xrpSyncData.addressEntity.getPath()));
                    }
                });
                break;
            case POLKADOT_JS:
                SyncInfo info = syncInfoList.get(0);
                syncViewModel.generateSyncPolkadotjs(info).observe(this, s -> {
                    if (!TextUtils.isEmpty(s)) {
                        mBinding.derivationPattern.setVisibility(View.VISIBLE);
                        mBinding.addressData.setVisibility(View.GONE);
                        mBinding.chain.setVisibility(View.VISIBLE);
                        mBinding.chain.setText(info.getName());
                        mBinding.fromPath.setText("Derivation Path:  " + info.getPath());
                        mBinding.dynamicQrcodeLayout.qrcode.disableMultipart();
                        mBinding.dynamicQrcodeLayout.qrcode.setData(s);
                    }
                });
                break;
            case METAMASK:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                syncViewModel.getChainsMutableLiveData().observe(this, ethAccount -> {
                    if (ethAccount == null) return;
                    if (isRefreshing) return;
                    isRefreshing = true;
                    Utilities.setCurrentEthAccount(mActivity, ethAccount.getCode());
                    mBinding.chain.setText(ethAccount.getName());
                    URLiveData = syncViewModel.generateSyncMetamaskUR(ethAccount);
                    URLiveData.observe(this, urData -> {
                        if (urData != null) {
                            mBinding.dynamicQrcodeLayout.qrcode.displayUR(urData);
                            mBinding.addressData.setVisibility(View.GONE);
                            mBinding.derivationPattern.setVisibility(View.VISIBLE);
                            mBinding.fromPath.setText(ethAccount.getName() + " Derivation Path: " + ethAccount.getDisplayPath());
                        }
                        URLiveData.removeObservers(this);
                    });
                });
                break;
            case CORE_WALLET:
            case BIT_KEEP:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                if (isRefreshing) return;
                isRefreshing = true;
                ETHAccount ethAccount = ETHAccount.ofCode(Utilities.getCurrentEthAccount(mActivity));
                CoreWalletViewModel coreWalletViewModel = ViewModelProviders.of(mActivity).get(CoreWalletViewModel.class);
                URLiveData = coreWalletViewModel.GenerateSyncData();
                URLiveData.observe(this, ur -> {
                    if (ur != null) {
                        mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                        mBinding.addressData.setVisibility(View.GONE);
                        mBinding.derivationPattern.setVisibility(View.VISIBLE);
                        mBinding.fromPath.setText(ethAccount.getDisplayPath());
                    }
                    URLiveData.removeObservers(this);
                });
                break;
            case ARConnect:
                ArweaveViewModel arweaveViewModel = ViewModelProviders.of(mActivity).get(ArweaveViewModel.class);
                URLiveData = arweaveViewModel.generateSyncData();
                URLiveData.observe(this, ur -> {
                    if (ur != null) {
                        mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                        mBinding.addressData.setVisibility(View.GONE);
                        mBinding.derivationPattern.setVisibility(View.VISIBLE);
                        mBinding.fromPath.setText(Coins.AR.getAccounts()[0]);
                    }
                    URLiveData.removeObservers(this);
                });
                break;
            case SOLANA:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                syncViewModel.getSolAccountMutableLiveData().observe(this, solAccount -> {
                    if (solAccount == null) return;
                    if (isRefreshing) return;
                    isRefreshing = true;
                    Utilities.setCurrentSolAccount(mActivity, solAccount.getCode());
                    URLiveData = syncViewModel.generateSyncURBySyncInfo(syncInfoList);
                    URLiveData.observe(this, urData -> {
                        if (urData != null) {
                            mBinding.dynamicQrcodeLayout.qrcode.displayUR(urData);
                            mBinding.derivationPattern.setVisibility(View.VISIBLE);
                            mBinding.addressData.setVisibility(View.GONE);
                            if (syncInfoList.size() == 1) {
                                mBinding.fromPath.setText(syncInfoList.get(0).getPath().toLowerCase());
                            } else {
                                String code = Utilities.getCurrentSolAccount(mActivity);
                                SOLAccount account = SOLAccount.ofCode(code);
                                mBinding.fromPath.setText(account.getDisplayPath().toLowerCase());
                            }
                        }
                        URLiveData.removeObservers(this);
                    });

                });
                break;
            case NEAR:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                syncViewModel.getNearAccountMutableLiveData().observe(this, nearAccount -> {
                    if (nearAccount == null) return;
                    if (isRefreshing) return;
                    isRefreshing = true;
                    Utilities.setCurrentNearAccount(mActivity, nearAccount.getCode());
                    URLiveData = syncViewModel.generateSyncURBySyncInfo(syncInfoList);
                    URLiveData.observe(this, urData -> {
                        if (urData != null) {
                            mBinding.dynamicQrcodeLayout.qrcode.displayUR(urData);
                            mBinding.derivationPattern.setVisibility(View.VISIBLE);
                            mBinding.addressData.setVisibility(View.GONE);
                            if (syncInfoList.size() == 1) {
                                mBinding.fromPath.setText(syncInfoList.get(0).getPath().toLowerCase());
                            } else {
                                String code = Utilities.getCurrentNearAccount(mActivity);
                                NEARAccount account = NEARAccount.ofCode(code);
                                mBinding.fromPath.setText(account.getDisplayPath().toLowerCase());
                            }
                        }
                        URLiveData.removeObservers(this);
                    });

                });
                break;
            case APTOS:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                URLiveData = syncViewModel.generateSyncURBySyncInfo(syncInfoList);
                URLiveData.observe(this, urData -> {
                    if (urData != null) {
                        mBinding.dynamicQrcodeLayout.qrcode.displayUR(urData);
                        mBinding.derivationPattern.setVisibility(View.VISIBLE);
                        mBinding.addressData.setVisibility(View.GONE);
                        if (syncInfoList.size() == 1) {
                            mBinding.fromPath.setText(syncInfoList.get(0).getPath().toLowerCase());
                        } else {
                            mBinding.fromPath.setText("m/44'/637'/*'/0'/0'");
                        }
                    }
                    URLiveData.removeObservers(this);
                });
                break;
            case KEPLR_WALLET:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                if (isRefreshing) return;
                isRefreshing = true;
                KeplrWalletViewModel keplrWalletViewModel = ViewModelProviders.of(mActivity).get(KeplrWalletViewModel.class);
                URLiveData = keplrWalletViewModel.generateSyncData();
                URLiveData.observe(this, ur -> {
                    if (ur != null) {
                        mBinding.dynamicQrcodeLayout.qrcode.displayUR(ur);
                        mBinding.addressData.setVisibility(View.GONE);
                        mBinding.derivationPattern.setVisibility(View.VISIBLE);
                        mBinding.fromPath.setText("m/44'/*'/0'/0/0");
                    }
                    URLiveData.removeObservers(this);
                });
                break;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isRefreshing = false;
    }

    private String generateXrpToolsSyncData(SyncViewModel.XrpSyncData xrpSyncData) {
        JSONObject object = new JSONObject();
        try {
            object.put("address", xrpSyncData.addressEntity.getAddressString())
                    .put("pubkey", xrpSyncData.pubkey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Hex.toHexString(object.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
