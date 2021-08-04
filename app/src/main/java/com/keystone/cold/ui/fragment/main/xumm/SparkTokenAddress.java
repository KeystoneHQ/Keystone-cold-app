/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.cold.ui.fragment.main.xumm;

import static com.keystone.cold.ui.fragment.main.keystone.TxConfirmFragment.KEY_TX_DATA;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.keystone.coinlib.exception.CoinNotFindException;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.cold.R;
import com.keystone.cold.databinding.CommonModalBinding;
import com.keystone.cold.databinding.SparkTokenAddressBinding;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.keystone.cold.ui.fragment.main.scan.scanner.exceptions.UnExpectedQRException;
import com.keystone.cold.ui.modal.ModalDialog;
import com.keystone.cold.viewmodel.SparkTokenViewModel;
import com.keystone.cold.viewmodel.WatchWallet;
import com.keystone.cold.viewmodel.exceptions.XfpNotMatchException;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class SparkTokenAddress extends BaseFragment<SparkTokenAddressBinding> {

    @Override
    protected int setView() {
        return R.layout.spark_token_address;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.info.setOnClickListener(v -> showGuide());
        ViewModelProviders.of(this)
                .get(SparkTokenViewModel.class)
                .getAddress(data.getInt("index"))
                .observe(this, s -> {
                    String messageKey = "02000000000000000000000000" + s.substring(2).toUpperCase();
                    mBinding.messageKey.setText(messageKey);
                    mBinding.qr.qrcode.disableMultipart();
                    mBinding.qr.qrcode.setData(messageKey);
                });
        mBinding.scan.setOnClickListener(v -> scanQrCode());
    }

    private void scanQrCode() {
        ScannerViewModel scannerViewModel = ViewModelProviders.of(mActivity).get(ScannerViewModel.class);
        scannerViewModel.setState(new ScannerState(Collections.singletonList(ScanResultTypes.UR_BYTES)) {
            @Override
            public void handleScanResult(ScanResult result) throws Exception {
                if (result.getType().equals(ScanResultTypes.UR_BYTES)) {
                    if (handleXRPToolkit(result)) return;
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

            private boolean handleXRPToolkit(ScanResult result) throws JSONException, InvalidTransactionException {
                JSONObject object = new JSONObject(new String((byte[]) result.resolve(), StandardCharsets.UTF_8));
                if (object.has("TransactionType")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_TX_DATA, object.toString());
                    mFragment.navigate(R.id.action_to_xrpTxConfirmFragment, bundle);
                    return true;
                }
                throw new InvalidTransactionException("unknown qr code type");
            }
        });
        navigate(R.id.action_to_scanner);
    }

    private void showGuide() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(getString(R.string.spark_token_prepare_title));
        binding.subTitle.setText(R.string.spark_token_claim_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
