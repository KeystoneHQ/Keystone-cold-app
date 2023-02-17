package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx;

import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.viewmodel.tx.BaseTxViewModel;

public abstract class ReviewTransactionFragment<T, V extends BaseTxViewModel<T>> extends ConfirmTransactionFragment<T, V> {

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.sign.setVisibility(View.GONE);
        mBinding.toolbarTitle.setText(R.string.signing_history);
    }


    @Override
    protected void onSignSuccess() {

    }
}
