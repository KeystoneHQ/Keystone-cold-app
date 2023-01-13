package com.keystone.cold.remove_wallet_mode.ui.model;

import android.util.Pair;

import androidx.databinding.ObservableField;

import java.util.List;

public class PathPatternItem {
    private final String code;
    private final String pathPattern;
    private final String patternName;
    private final boolean isRecommend;
    private final List<Pair<String, String>> pairs;
    private final ObservableField<Boolean> isSelect = new ObservableField<>();

    public PathPatternItem(String code, String pathPattern, String patternName, boolean isRecommend, List<Pair<String, String>> pairs) {
        this.code = code;
        this.pathPattern = pathPattern;
        this.patternName = patternName;
        this.isRecommend = isRecommend;
        this.pairs = pairs;
        // default value of "isSelected" is the value of "isRecommend"
        this.isSelect.set(isRecommend);
    }

    public String getCode() {
        return code;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public String getPatternName() {
        return patternName;
    }

    public boolean isRecommend() {
        return isRecommend;
    }

    public List<Pair<String, String>> getPairs() {
        return pairs;
    }

    public ObservableField<Boolean> getIsSelect() {
        return isSelect;
    }
}
