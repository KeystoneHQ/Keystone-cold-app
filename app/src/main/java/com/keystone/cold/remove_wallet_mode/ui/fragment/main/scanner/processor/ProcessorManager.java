package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;

public class ProcessorManager {

    public ProcessorManager() {
    }

    public static Destination handleScanResult(ScanResult scanResult) throws BaseException {
        if (scanResult.getType().equals(ScanResultTypes.PLAIN_TEXT)) {
            return new TextProcessor().run(scanResult);
        } else if (scanResult.getType().equals(ScanResultTypes.UOS)) {
            return new UOSProcessor().run(scanResult);
        }
        return new URProcessor().run(scanResult);
    }
}
