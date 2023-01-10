package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.UnimplementedException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;

public class TextProcessor implements Processor {
    @Override
    public Destination run(ScanResult r) throws BaseException {
        throw UnimplementedException.newInstance();
    }
}
