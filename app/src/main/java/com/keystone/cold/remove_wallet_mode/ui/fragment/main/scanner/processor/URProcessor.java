package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.UnimplementedException;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;

public class URProcessor implements Processor{
    @Override
    public ProcessorManager.ProcessorDestination run(ScanResult r) throws BaseException {
        if (r.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
            new ETHSignRequestProcessor().run(r.resolve());
        }
        throw UnimplementedException.newInstance();
    }

    interface URResolver {
        ProcessorManager.ProcessorDestination run(Object object);
    }

    private static class ETHSignRequestProcessor implements URResolver {

        @Override
        public ProcessorManager.ProcessorDestination run(Object object) {
            EthSignRequest signRequest = (EthSignRequest) object;
            return null;
        }
    }
}
