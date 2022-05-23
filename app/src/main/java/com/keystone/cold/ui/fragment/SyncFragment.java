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

import static com.keystone.cold.ui.fragment.main.solana.AddressSyncFragment.DERIVATION_PATH_KEY;
import static com.keystone.cold.ui.fragment.setup.SyncWatchWalletGuide.getSyncWatchWalletGuide;
import static com.keystone.cold.ui.fragment.setup.SyncWatchWalletGuide.getSyncWatchWalletGuideTitle;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.SyncFragmentBinding;
import com.keystone.cold.ui.MainActivity;
import com.keystone.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.SyncViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.UR;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SyncFragment extends SetupVaultBaseFragment<SyncFragmentBinding> {

    public static final String TAG = "SyncFragment";
    private SyncViewModel syncViewModel;
    private WatchWallet watchWallet;
    private String coinCode;
    private boolean fromSyncGuide = false;
    private boolean isRefreshing = false;

    private MutableLiveData<UR> URLiveData;

    private List<Pair<String, String>> solSyncInfo = new ArrayList<>();

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

            String syncPaths = getArguments().getString(DERIVATION_PATH_KEY);
            if (!TextUtils.isEmpty(syncPaths)) {
                solSyncInfo.addAll(collectSyncInfo(syncPaths));
            }
        }
        if (!fromSyncGuide) {
            mBinding.complete.setVisibility(View.GONE);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.setTitle("");
        mBinding.complete.setOnClickListener(v -> {
            if (mActivity instanceof MainActivity) {
                if (watchWallet == WatchWallet.POLKADOT_JS && coinCode.equals(Coins.DOT.coinCode())) {
                    Bundle bundle = getArguments();
                    bundle.putString("coinCode", Coins.KSM.coinCode());
                    navigate(R.id.action_to_syncWatchWalletGuide, bundle);
                } else if (watchWallet == WatchWallet.METAMASK) {
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

    private List<Pair<String, String>> collectSyncInfo(String syncInfo) {
        List<Pair<String, String>> infoList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(syncInfo);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                String path = jsonObject.getString("path");
                String address = jsonObject.getString("address");
                Pair<String, String> pathAddressPair = Pair.create(path, address);
                infoList.add(pathAddressPair);
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return infoList;
    }

    private void setupUIWithWatchWallet() {
        mBinding.info.setOnClickListener(v -> {
            switch (watchWallet) {
                case METAMASK:
                case SOLANA:
                case XRP_TOOLKIT:
                case POLKADOT_JS:
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
            case POLKADOT_JS:
                mBinding.hint.setText(R.string.sync_with_polkadot_js);
                mBinding.chain.setVisibility(View.VISIBLE);
                if (coinCode.equals(Coins.DOT.coinCode())) {
                    mBinding.chain.setText(Coins.DOT.coinName());
                } else if (coinCode.equals(Coins.KSM.coinCode())) {
                    mBinding.chain.setText(Coins.KSM.coinName());
                }
                break;
            case XRP_TOOLKIT:
                mBinding.hint.setText(R.string.sync_with_xrp_toolkit);
                mBinding.address.setVisibility(View.VISIBLE);
                break;
            case METAMASK:
            case SOLANA:
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
                syncViewModel.generateSyncPolkadotjs(coinCode).observe(this, s -> {
                    if (!TextUtils.isEmpty(s)) {
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
            case SOLANA:
                if (URLiveData != null) {
                    URLiveData.removeObservers(this);
                }
                syncViewModel.getSolAccountMutableLiveData().observe(this, solAccount -> {
                    if (solAccount == null) return;
                    if (isRefreshing) return;
                    isRefreshing = true;
                    Utilities.setCurrentSolAccount(mActivity, solAccount.getCode());
                    URLiveData = syncViewModel.generateSyncSolanaUR(solSyncInfo);
                    URLiveData.observe(this, urData -> {
                        if (urData != null) {
                            mBinding.dynamicQrcodeLayout.qrcode.displayUR(urData);
                            mBinding.derivationPattern.setVisibility(View.VISIBLE);
                            mBinding.addressData.setVisibility(View.GONE);
                            if (solSyncInfo.size() == 1) {
                                mBinding.fromPath.setText(solSyncInfo.get(0).first.toLowerCase());
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
