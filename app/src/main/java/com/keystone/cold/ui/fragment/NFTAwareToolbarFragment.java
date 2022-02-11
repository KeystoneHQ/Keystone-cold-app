package com.keystone.cold.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.NftAwareToolbarBinding;

public class NFTAwareToolbarFragment extends BaseFragment<NftAwareToolbarBinding> {

    private final boolean canGoBack;

    public NFTAwareToolbarFragment(boolean canGoBack) {
        super();
        this.canGoBack = canGoBack;
    }

    @Override
    protected int setView() {
        return R.layout.nft_aware_toolbar;
    }

    @Override
    protected void init(View view) {
        Bitmap nftImage = Utilities.getNFTAvatarBitmap(mActivity);
        if (nftImage != null) {
            mBinding.toolbarTitle.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.nft_toolbar_height));
            mBinding.toolbar.setLayoutParams(params);
            mBinding.nftContainer.setVisibility(View.VISIBLE);
            mBinding.nftImage.setImageBitmap(nftImage);
            mBinding.divider.setVisibility(View.GONE);
        }
        if (canGoBack) {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        } else {
            mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
