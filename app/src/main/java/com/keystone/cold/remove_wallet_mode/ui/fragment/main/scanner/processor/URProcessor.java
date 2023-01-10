package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumConfirmTransactionFragment.FEE_MARKET_TRANSACTION;
import static com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumConfirmTransactionFragment.LEGACY_TRANSACTION;

import android.os.Bundle;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.Utilities;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.UnimplementedException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.UnknownQrCodeException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.XfpNotMatchException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidETHAccountException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
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
        public Destination run(Object object) throws BaseException {
            EthSignRequest ethSignRequest = (EthSignRequest) object;
            Bundle bundle = new Bundle();
            ByteBuffer uuidBuffer = ByteBuffer.wrap(ethSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = ethSignRequest.getDerivationPath();
            String requestMFP = Hex.toHexString(ethSignRequest.getMasterFingerprint());
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, Hex.toHexString(ethSignRequest.getSignData()));
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);

            ETHAccount current = ETHAccount.ofCode(Utilities.getCurrentEthAccount(MainApplication.getApplication()));
            ETHAccount target = ETHAccount.getAccountByPath(hdPath);
            if (target == null) {
                throw new InvalidTransactionException("test", "unknown hd path");
            }
            if (!target.equals(current)) {
                if (!current.isChildrenPath(hdPath)) {
                    //standard and ledger_live has overlap of 1st address
                    throw new InvalidETHAccountException("test", "not expected ETH account", current, target);
                }
            }

            String MFP = new GetMasterFingerprintCallable().call();

            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw new XfpNotMatchException("", "Master fingerprint not match");
            }
            if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TRANSACTION.getType())) {
                bundle.putInt(BundleKeys.ETH_TX_TYPE_KEY, LEGACY_TRANSACTION);
                return new Destination(R.id.action_to_ethTxConfirmFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_DATA.getType())) {
                return new Destination(R.id.action_to_ethSignTypedDataFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.PERSONAL_MESSAGE.getType())) {
                return new Destination(R.id.action_to_ethSignMessageFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_TRANSACTION.getType())) {
                byte[] typedTransaction = ethSignRequest.getSignData();
                byte type = typedTransaction[0];
                switch (type) {
                    case 0x02:
                        bundle.putInt(BundleKeys.ETH_TX_TYPE_KEY, FEE_MARKET_TRANSACTION);
                        return new Destination(R.id.action_to_ethFeeMarketTxConfirmFragment, bundle);
                    default:
                        throw new UnknownQrCodeException("test", "unknown transaction!");
                }
            }
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
