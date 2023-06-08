package com.keystone.cold.remove_wallet_mode.ui.fragment.connect_wallet;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentKeyRequestApproveBinding;
import com.keystone.cold.databinding.ItemKeyRequestBinding;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.wallet.Wallet;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.ui.fragment.BaseFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KeyRequestApproveFragment extends BaseFragment<FragmentKeyRequestApproveBinding> {

    @Override
    protected int setView() {
        return R.layout.fragment_key_request_approve;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        KeyDerivationRequest request = (KeyDerivationRequest) data.getSerializable(BundleKeys.KEY_REQUEST_KEY);
        String walletId = data.getString(BundleKeys.WALLET_ID_KEY);
        Wallet wallet = Wallet.getWalletByIdOrUnknown(request.getOrigin() == null ? walletId : request.getOrigin());
        if (wallet.equals(Wallet.UNKNOWNWALLET)) {
            mBinding.cooperateImageContainer.setVisibility(View.GONE);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            navigateUp();
        });
        mBinding.description.setText(getString(R.string.wallet_key_request, wallet.getWalletName()));
        KeyRequestAdapter adapter = new KeyRequestAdapter(mActivity);
        mBinding.keyRequestList.setAdapter(adapter);
        adapter.setItems(request.getSchemas());
        mBinding.reject.setOnClickListener(v -> {
            navigateUp();
        });
        mBinding.approve.setOnClickListener(v -> {
            data.putString(BundleKeys.WALLET_ID_KEY, wallet.getWalletId());
            data.putInt(BundleKeys.SYNC_ACTION_MODE_KEY, SyncFragment.SyncActionMode.KeyRequest.ordinal());
            navigate(R.id.action_to_syncFragment, data);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    public static class Schema implements Serializable {
        private final String path;
        private final int curve; //0: secp256k1, 1: ed25519
        private final int algo; //0: slip10, 1: bip32-ed25519

        public Schema(String path, int curve, int algo) {
            this.path = path;
            this.curve = curve;
            this.algo = algo;
        }

        public String getPath() {
            return path;
        }

        public int getCurve() {
            return curve;
        }

        public int getAlgo() {
            return algo;
        }

        @Override
        public String toString() {
            return "Schema{" +
                    "path='" + path + '\'' +
                    ", curve=" + curve +
                    ", algo=" + algo +
                    '}';
        }
    }

    public static class KeyDerivationRequest implements Serializable {
        private final List<Schema> schemas = new ArrayList<>();
        private final String origin;

        public KeyDerivationRequest() {
            this.origin = null;
        }

        public KeyDerivationRequest(String origin) {
            this.origin = origin;
        }

        public void addSchema(Schema e) {
            this.schemas.add(e);
        }

        public List<Schema> getSchemas() {
            return schemas;
        }

        public String getOrigin() {
            return origin;
        }
    }

    private static class KeyRequestAdapter extends BaseBindingAdapter<Schema, ItemKeyRequestBinding> {

        public KeyRequestAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_key_request;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemKeyRequestBinding binding = DataBindingUtil.getBinding(holder.itemView);
            onBindItem(binding, this.items.get(position), position);
        }

        @Override
        protected void onBindItem(ItemKeyRequestBinding binding, Schema item) {
            //no usage
        }

        private void onBindItem(ItemKeyRequestBinding binding, Schema item, int position) {
            String text = String.format("Account %d: %s", position, item.getPath());
            binding.text.setText(text);
        }
    }
}
