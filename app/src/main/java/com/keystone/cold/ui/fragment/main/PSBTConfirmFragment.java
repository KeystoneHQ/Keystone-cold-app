package com.keystone.cold.ui.fragment.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.databinding.PsbtConfirmFragmentBinding;
import com.keystone.cold.databinding.TxDetailItemBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;
import com.keystone.cold.viewmodel.PSBTViewModel;

public class PSBTConfirmFragment extends BaseFragment<PsbtConfirmFragmentBinding> {
    PSBTViewModel psbtViewModel;
    PSBTViewModel.PSBT psbt;

    @Override
    protected int setView() {
        return R.layout.psbt_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = requireArguments();

        String psbtB64 = bundle.getString("psbt");

        psbtViewModel = ViewModelProviders.of(this).get(PSBTViewModel.class);

        try {
            psbt = psbtViewModel.parsePsbtBase64(psbtB64);
            String myMasterFingerprint = new GetMasterFingerprintCallable().call();
            psbt.validate(myMasterFingerprint);
        } catch (InvalidTransactionException e) {
            this.alert("Invalid Transaction", e.getMessage(), this::navigateUp);
        }

        mBinding.setPsbt(psbt);
        mBinding.txDetail.qr.setVisibility(View.GONE);

        PSBTInputAdapter inputAdapter = new PSBTInputAdapter(mActivity);
        inputAdapter.setItems(psbt.getInputs());
        mBinding.txDetail.fromList.setAdapter(inputAdapter);

        PSBTOutputAdapter outputAdapter = new PSBTOutputAdapter(mActivity);
        outputAdapter.setItems(psbt.getOutputs());
        mBinding.txDetail.toList.setAdapter(outputAdapter);

        mBinding.txDetail.fee.setText(psbt.getFeeText());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private static class PSBTInputAdapter extends BaseBindingAdapter<PSBTViewModel.PSBT.Input, TxDetailItemBinding> {
        private int position;
        public PSBTInputAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.tx_detail_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TxDetailItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            this.position = holder.getAdapterPosition();
            onBindItem(binding, this.items.get(position));
        }

        @Override
        protected void onBindItem(TxDetailItemBinding binding, PSBTViewModel.PSBT.Input item) {
            binding.label.setText("Input" + " " + this.position);
            binding.info.setText(item.getValueText() + "\n" + item.getAddress());
        }
    }

    private static class PSBTOutputAdapter extends BaseBindingAdapter<PSBTViewModel.PSBT.Output, TxDetailItemBinding> {
        private int position;
        public PSBTOutputAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.tx_detail_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TxDetailItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            this.position = holder.getAdapterPosition();
            onBindItem(binding, this.items.get(position));
        }

        @Override
        protected void onBindItem(TxDetailItemBinding binding, PSBTViewModel.PSBT.Output item) {
            binding.label.setText("Output" + " " + this.position);
            String myMasterFingerprint = new GetMasterFingerprintCallable().call();
            if (item.isChange(myMasterFingerprint)) {
                binding.info.setText(item.getValueText() + "\n" + item.getAddress() + "\n" + item.getChangePath());
                binding.change.setVisibility(View.VISIBLE);
            }
        }
    }
}
