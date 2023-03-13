package com.keystone.cold.remove_wallet_mode.ui.fragment.unlock;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.databinding.FragmentNftAwareToolbarBinding;
import com.keystone.cold.ui.fragment.BaseFragment;

public class NFTAwareToolbarFragment extends BaseFragment<FragmentNftAwareToolbarBinding> {

    private final boolean canGoBack;
    private final String nonNFTTitle;

    private boolean showNFT = false;
    private int nftWidthDimensionId;
    private int nftMarginTopDimensionId;

    public NFTAwareToolbarFragment(boolean canGoBack, String nonNFTTitle) {
        super();
        this.canGoBack = canGoBack;
        this.nonNFTTitle = nonNFTTitle;
        this.nftWidthDimensionId = R.dimen.dp_188;
        this.nftMarginTopDimensionId = R.dimen.dp_1;
    }

    public NFTAwareToolbarFragment(boolean canGoBack, String nonNFTTitle, int nftWidthDimensionId, int nftMarginTopDimensionId) {
        super();
        this.canGoBack = canGoBack;
        this.nonNFTTitle = nonNFTTitle;
        this.nftWidthDimensionId = nftWidthDimensionId;
        this.nftMarginTopDimensionId = nftMarginTopDimensionId;
    }

    public boolean isShowNFT() {
        return this.showNFT;
    }

    @Override
    protected int setView() {
        return R.layout.fragment_nft_aware_toolbar;
    }

    @Override
    protected void init(View view) {
        Bitmap nftImage = Utilities.getNFTAvatarBitmap(mActivity);

        if (nftImage != null) {
            this.showNFT = true;
            mBinding.toolbar.setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mBinding.nftContainer.getLayoutParams();
            layoutParams.height = (int) getResources().getDimension(this.nftWidthDimensionId);
            layoutParams.width = (int) getResources().getDimension(this.nftWidthDimensionId);
            layoutParams.topMargin = (int) getResources().getDimension(this.nftMarginTopDimensionId);
            mBinding.nftContainer.setLayoutParams(layoutParams);
            mBinding.nftContainer.setRadius((int) (getResources().getDimension(this.nftWidthDimensionId) / 2));
            mBinding.nftContainer.setVisibility(View.VISIBLE);
            mBinding.nftImage.setImageBitmap(nftImage);
            mBinding.divider.setVisibility(View.GONE);
        } else {
            mBinding.toolbarTitle.setText(nonNFTTitle);
            if (nonNFTTitle == null) {
                mBinding.toolbar.setVisibility(View.GONE);
                mBinding.divider.setVisibility(View.GONE);
            }
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
