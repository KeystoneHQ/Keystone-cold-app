package com.keystone.coinlib.selector;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectorLoadManager {
    private String methodSignature;

    private static List<SelectorStoreEngine> selectorStoreEngineList;

    static {
        selectorStoreEngineList = Arrays.asList(new TFCardSelectorStore());
    }


    public SelectorLoadManager(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public List<MethodSignature> loadSelector() {
        List<MethodSignature> methodSignatureList = new ArrayList<>();
        if (TextUtils.isEmpty(methodSignature)) {
            return methodSignatureList;
        }
        if (methodSignature.toLowerCase().startsWith("0x")) {
            methodSignature = methodSignature.substring(2);
        }
        String fourBytes = methodSignature.substring(0, 8);

        for (SelectorStoreEngine selectorStoreEngine : selectorStoreEngineList) {
            methodSignatureList.addAll(selectorStoreEngine.load(fourBytes));
            if (!methodSignatureList.isEmpty()) {
                return methodSignatureList;
            }
        }
        return methodSignatureList;
    }
}
