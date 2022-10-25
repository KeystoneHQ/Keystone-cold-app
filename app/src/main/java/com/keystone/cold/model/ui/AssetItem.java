package com.keystone.cold.model.ui;

public class AssetItem {

    private final String title;
    private final String subTitle;
    private final String coinCode;
    private final String tag;
    private final boolean hasTag;

    public AssetItem(String title, String subTitle, String coinCode, String tag, boolean hasTag) {
        this.title = title;
        this.subTitle = subTitle;
        this.coinCode = coinCode;
        this.tag = tag;
        this.hasTag = hasTag;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public String getTag() {
        return tag;
    }

    public boolean isHasTag() {
        return hasTag;
    }
}
