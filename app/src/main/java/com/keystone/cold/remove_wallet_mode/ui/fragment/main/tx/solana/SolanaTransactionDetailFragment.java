package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentSolanaTxBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SolanaTransactionDetailFragment extends BaseFragment<FragmentSolanaTxBinding> {

    private LiveData<JSONObject> parsedTx;
    private String field;

    static Fragment newInstance(@NonNull Bundle bundle, LiveData<JSONObject> parsedTx, String field) {
        SolanaTransactionDetailFragment fragment = new SolanaTransactionDetailFragment();
        fragment.setArguments(bundle);
        fragment.parsedTx = parsedTx;
        fragment.field = field;
        return fragment;
    }


    @Override
    protected int setView() {
        return R.layout.fragment_solana_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.setCoinCode(Coins.SOL.coinCode());
        mBinding.setCheckInfoTitle(Coins.SOL.coinName());
        parsedTx.observe(this, jsonObject -> {
            if (jsonObject != null) {
                if (jsonObject.optBoolean("record")) {
                    mBinding.checkInfoLayout.llHint.setVisibility(View.GONE);
                }
                InstructionAdapter adapter = new InstructionAdapter(getContext(), field);
                List<JSONObject> list = new ArrayList<>();
                try {
                    JSONArray instructions = jsonObject.getJSONArray("instructions");
                    int length = instructions.length();
                    for (int i = 0; i < length; i++) {
                        if ("overview".equals(field)) {
                            if (isSupport(instructions.getJSONObject(i))) {
                                list.add(instructions.getJSONObject(i));
                            }
                        } else {
                            list.add(instructions.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!list.isEmpty()) {
                    adapter.setItems(list);
                    mBinding.instructions.setAdapter(adapter);
                    mBinding.rlListContainer.setVisibility(View.VISIBLE);
                } else {
                    mBinding.errorPage.setVisibility(View.VISIBLE);
                }
            }
            parsedTx.removeObservers(this);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private boolean isSupport(JSONObject jsonObject) throws JSONException {
        return !(jsonObject.get("readable") instanceof String);
    }
}
