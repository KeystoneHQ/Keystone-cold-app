package com.keystone.cold.remove_wallet_mode.ui.fragment.main;

import android.os.Bundle;
import android.view.View;

import com.keystone.cold.R;
import com.keystone.cold.databinding.FragmentFaqBinding;
import com.keystone.cold.remove_wallet_mode.ui.modal.QRDialog;
import com.keystone.cold.ui.fragment.BaseFragment;

public class FAQFragment extends BaseFragment<FragmentFaqBinding> {

    @Override
    protected int setView() {
        return R.layout.fragment_faq;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.rlForMore.setOnClickListener(v -> QRDialog.show(mActivity, getString(R.string.faq_link), getString(R.string.faq)));

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        String[] tittleArray = getResources().getStringArray(R.array.faq_list_tittle);
        String[] contentArray = getResources().getStringArray(R.array.faq_list_content);
        mBinding.faqList.setData(tittleArray, contentArray);
    }
}
