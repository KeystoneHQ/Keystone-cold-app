package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import android.os.Bundle;

import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.UnimplementedException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.XfpNotMatchException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.util.AptosTransactionHelper;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignRequest;

import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public class URProcessor implements Processor{
    @Override
    public Destination run(ScanResult r) throws BaseException {
        if (r.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
            return new ETHSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_APTOS_SIGN_REQUEST)) {
            return new AptosSignRequestProcessor().run(r.resolve());
        } else {
            throw UnimplementedException.newInstance();
        }
    }

    interface URResolver {
        Destination run(Object object) throws BaseException;
    }

    private static class ETHSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) {
            EthSignRequest signRequest = (EthSignRequest) object;
            return null;
        }
    }


    private static class AptosSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            AptosSignRequest aptosSignRequest = (AptosSignRequest) object;
            String requestMFP = Hex.toHexString(aptosSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw XfpNotMatchException.newInstance();
            }
            ByteBuffer uuidBuffer = ByteBuffer.wrap(aptosSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = aptosSignRequest.getDerivationPath();
            String signData = Hex.toHexString(aptosSignRequest.getSignData());
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, signData);
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);
            AptosSignRequest.DataType dataType = aptosSignRequest.getType();
            switch (dataType) {
                case MESSAGE:
                    return new Destination(R.id.action_to_aptosMessageFragment, bundle);
                case SINGLE:
                    AptosTransactionHelper.Type type = AptosTransactionHelper.judgeDataType(signData);
                    if (type == AptosTransactionHelper.Type.MESSAGE) {
                        return new Destination(R.id.action_to_aptosMessageFragment, bundle);
                    } else {
                        return new Destination(R.id.action_to_aptosConfirmTransactionFragment, bundle);
                    }
                case MULTI:
            }
            throw UnimplementedException.newInstance();
        }
    }
}
