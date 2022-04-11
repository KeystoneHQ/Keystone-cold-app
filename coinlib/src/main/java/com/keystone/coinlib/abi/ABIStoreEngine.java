package com.keystone.coinlib.abi;

import java.util.List;

public interface ABIStoreEngine {
    List<Contract> load(String address);
}
