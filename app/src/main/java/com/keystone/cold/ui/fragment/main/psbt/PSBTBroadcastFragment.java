package com.keystone.cold.ui.fragment.main.psbt;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.model.Tx;
import com.keystone.cold.ui.fragment.main.keystone.BroadcastTxFragment;
import com.keystone.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;

import org.spongycastle.util.encoders.Base64;

public class PSBTBroadcastFragment extends BroadcastTxFragment {

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        String txId = data.getString(KEY_TXID);
        DataRepository repository = MainApplication.getApplication().getRepository();
        repository.loadTx(txId).observe(this, v -> {
            String signedPSBTB64 = v.getSignedHex();
            CryptoPSBT cryptoPSBT = new CryptoPSBT(Base64.decode(signedPSBTB64));
            mBinding.qrcodeLayout.qrcode.displayUR(cryptoPSBT.toUR());
        });

        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetListFragment, false));
        if (watchWallet.equals(WatchWallet.CORE_WALLET)) {
            mBinding.broadcastHint.setText(R.string.sync_with_core_wallet);
        }
        else if (watchWallet.equals(WatchWallet.BIT_KEEP)) {
            mBinding.broadcastHint.setText(R.string.sync_with_bit_keep);
        }
        else {
            mBinding.broadcastHint.setText(R.string.sync_with_watch_only);
        }
        mBinding.icon.setImageDrawable(mActivity.getDrawable(R.drawable.coin_btc));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    protected String getSignTxJson(Tx txEntity) {
        return super.getSignTxJson(txEntity);
    }
}
