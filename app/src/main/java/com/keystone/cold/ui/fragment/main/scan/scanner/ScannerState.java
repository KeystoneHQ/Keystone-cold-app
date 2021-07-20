package com.keystone.cold.ui.fragment.main.scan.scanner;

import com.keystone.cold.databinding.ScannerFragmentBinding;

import java.security.InvalidParameterException;
import java.util.List;

public class ScannerState {
    private List<ScanResultTypes> desiredResults;
    protected ScannerFragment mFragment;

    public ScannerState(List<ScanResultTypes> desiredResults) {
        this.desiredResults = desiredResults;
    }

    public List<ScanResultTypes> getDesiredResults() {
        return desiredResults;
    }

    public void handleScanResult(ScanResult result) throws Exception {
    }

    public void style(ScannerFragmentBinding binding) {
    }

    public void bindFragment(ScannerFragment fragment) {
        this.mFragment = fragment;
    }

    public boolean handleException(Exception e) {
        if (this.mFragment == null) {
            throw new InvalidParameterException("No ScannerFragment bond");
        }
        return false;
    }
}
