package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ripple;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentRippleTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import org.json.JSONObject;

public class RippleTransactionDetailFragment extends BaseFragment<FragmentRippleTxBinding> {
    private MutableLiveData<JSONObject> tx;

    public RippleTransactionDetailFragment(MutableLiveData<JSONObject> tx) {
        this.tx = tx;
    }

    public static RippleTransactionDetailFragment newInstance(Bundle bundle, MutableLiveData<JSONObject> tx) {
        RippleTransactionDetailFragment fragment = new RippleTransactionDetailFragment(tx);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_ripple_tx;
    }

    @Override
    protected void init(View view) {
        tx.observe(this, (v) -> {
            if (v == null) return;
            mBinding.checkInfo.setCoinCode(Coins.XRP.coinCode());
            mBinding.checkInfo.setTitle(Coins.XRP.coinCode());
            mBinding.container.setData(v);
            if (!v.optString("txHex").isEmpty()) {
                mBinding.qr.setVisibility(View.VISIBLE);
                mBinding.qrcode.qrcode.setData(v.optString("txHex"));
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
