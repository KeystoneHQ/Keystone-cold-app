package com.keystone.coinlib.selector;

import java.util.List;

public interface SelectorStoreEngine {

    List<MethodSignature> load(String signature);

}
