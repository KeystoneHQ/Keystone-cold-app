package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import android.os.Bundle;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;

public class ProcessorManager {
    public static class ProcessorDestination {
        public int id;
        public Bundle bundle;

        public ProcessorDestination(int id, Bundle bundle) {
            this.id = id;
            this.bundle = bundle;
        }
    }

    public ProcessorManager() {
    }

    public static ProcessorDestination handleScanResult(ScanResult scanResult) throws BaseException {
        if (scanResult.getType().equals(ScanResultTypes.PLAIN_TEXT)) {
            return new TextProcessor().run(scanResult);
        } else if (scanResult.getType().equals(ScanResultTypes.UOS)) {
            return new UOSProcessor().run(scanResult);
        }
        return new URProcessor().run(scanResult);
    }
}
