package com.keystone.cold.remove_wallet_mode.ui.fragment.main.scanner.processor;

import android.os.Bundle;

import com.keystone.coinlib.accounts.ETHAccount;
import com.keystone.coinlib.accounts.NEARAccount;
import com.keystone.coinlib.accounts.SOLAccount;
import com.keystone.cold.R;
import com.keystone.cold.callables.GetMasterFingerprintCallable;
import com.keystone.cold.protocol.ZipUtil;
import com.keystone.cold.protocol.parser.ProtoParser;
import com.keystone.cold.remove_wallet_mode.constant.BundleKeys;
import com.keystone.cold.remove_wallet_mode.exceptions.BaseException;
import com.keystone.cold.remove_wallet_mode.exceptions.UnimplementedException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.UnknownQrCodeException;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.XfpNotMatchException;
import com.keystone.cold.remove_wallet_mode.exceptions.tx.InvalidTransactionException;
import com.keystone.cold.remove_wallet_mode.helper.Destination;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.ethereum.EthereumTransaction;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.keystone.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.keystone.cold.util.AptosTransactionHelper;
import com.keystone.cold.util.SolMessageValidateUtil;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignRequest;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignRequest;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignRequest;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignRequest;
import com.sparrowwallet.hummingbird.registry.near.NearSignRequest;
import com.sparrowwallet.hummingbird.registry.solana.SolSignRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class URProcessor implements Processor {
    @Override
    public Destination run(ScanResult r) throws BaseException {
        if (r.getType().equals(ScanResultTypes.UR_ETH_SIGN_REQUEST)) {
            return new ETHSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_APTOS_SIGN_REQUEST)) {
            return new AptosSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_SOL_SIGN_REQUEST)) {
            return new SolSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_NEAR_SIGN_REQUEST)) {
            return new NearSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_CRYPTO_PSBT)) {
            return new CryptoPSBTProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_ARWEAVE_SIGN_REQUEST)) {
            return new ARweaveSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_COSMOS_SIGN_REQUEST)) {
            return new CosmosSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_EVM_SIGN_REQUEST)) {
            return new EvmSignRequestProcessor().run(r.resolve());
        } else if (r.getType().equals(ScanResultTypes.UR_BYTES)) {
            return new BytesProcessor().run(r.resolve());
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

            ETHAccount target = ETHAccount.getAccountByPath(hdPath);
            if (target == null) {
                throw new InvalidTransactionException("test", "unknown hd path");
            }

            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw new XfpNotMatchException("", "Master fingerprint not match");
            }
            if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TRANSACTION.getType())) {
                bundle.putInt(BundleKeys.ETH_TX_TYPE_KEY, EthereumTransaction.TransactionType.LEGACY.getType());
                return new Destination(R.id.action_to_ethereumConfirmTransactionFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_DATA.getType())) {
                return new Destination(R.id.action_to_ethereumSignTypedDataFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.PERSONAL_MESSAGE.getType())) {
                return new Destination(R.id.action_to_ethereumSignMessageFragment, bundle);
            } else if (ethSignRequest.getDataType().equals(EthSignRequest.DataType.TYPED_TRANSACTION.getType())) {
                byte[] typedTransaction = ethSignRequest.getSignData();
                byte type = typedTransaction[0];
                switch (type) {
                    case 0x02:
                        bundle.putInt(BundleKeys.ETH_TX_TYPE_KEY, EthereumTransaction.TransactionType.FEE_MARKET.getType());
                        return new Destination(R.id.action_to_ethereumConfirmTransactionFragment, bundle);
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
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, aptosSignRequest.getOrigin());
            AptosSignRequest.DataType dataType = aptosSignRequest.getType();
            switch (dataType) {
                case MESSAGE:
                    return new Destination(R.id.action_to_aptosSignMessageFragment, bundle);
                case SINGLE:
                    AptosTransactionHelper.Type type = AptosTransactionHelper.judgeDataType(signData);
                    if (type == AptosTransactionHelper.Type.MESSAGE) {
                        return new Destination(R.id.action_to_aptosSignMessageFragment, bundle);
                    } else {
                        return new Destination(R.id.action_to_aptosConfirmTransactionFragment, bundle);
                    }
                case MULTI:
                    throw new InvalidTransactionException("test", "Transaction type Multi not supported yet");
            }
            throw UnimplementedException.newInstance();
        }
    }

    private static class SolSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {

            SolSignRequest solSignRequest = (SolSignRequest) object;
            Bundle bundle = new Bundle();
            ByteBuffer uuidBuffer = ByteBuffer.wrap(solSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = solSignRequest.getDerivationPath();
            SOLAccount target = SOLAccount.getAccountByPath(hdPath);
            if (target == null) {
                throw new InvalidTransactionException("test", "unknown hd path");
            }
            String requestMFP = Hex.toHexString(solSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw XfpNotMatchException.newInstance();
            }
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            String signData = Hex.toHexString(solSignRequest.getSignData());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, signData);
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, solSignRequest.getOrigin());
            SolMessageValidateUtil.DataType dataType = SolMessageValidateUtil.judgeDataType(signData);
            switch (dataType) {
                case TRANSACTION:
                    return new Destination(R.id.action_to_solanaConfirmTransactionFragment, bundle);
                case MESSAGE:
                    return new Destination(R.id.action_to_solanaSignMessageFragment, bundle);
                case INVALIDATE:
                    throw new InvalidTransactionException("test", "unknown sign type");
            }
            throw UnimplementedException.newInstance();
        }
    }

    private static class NearSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {

            NearSignRequest nearSignRequest = (NearSignRequest) object;
            ByteBuffer uuidBuffer = ByteBuffer.wrap(nearSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = nearSignRequest.getDerivationPath();
            NEARAccount target = NEARAccount.getAccountByPath(hdPath);
            if (target == null) {
                throw new InvalidTransactionException("test", "unknown hd path");
            }
            String requestMFP = Hex.toHexString(nearSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw XfpNotMatchException.newInstance();
            }
            List<byte[]> signDataList = nearSignRequest.getSignDataList();
            List<String> signHexList = new ArrayList<>(signDataList.size());
            for (byte[] signData : signDataList) {
                signHexList.add(Hex.toHexString(signData));
            }
            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putSerializable(BundleKeys.SIGN_DATA_KEY, (Serializable) signHexList);
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, nearSignRequest.getOrigin());
            return new Destination(R.id.action_to_nearConfirmTransactionFragment, bundle);
        }
    }

    private static class CryptoPSBTProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            CryptoPSBT psbt = (CryptoPSBT) object;
            byte[] bytes = psbt.getPsbt();
            String psbtB64 = Base64.toBase64String(bytes);
            Bundle data = new Bundle();
            data.putString(BundleKeys.SIGN_DATA_KEY, psbtB64);
            return new Destination(R.id.action_to_bitcoinConfirmTransactionFragment, data);
        }
    }

    private static class ARweaveSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            ArweaveSignRequest arweaveSignRequest = (ArweaveSignRequest) object;
            Bundle bundle = new Bundle();
            ByteBuffer uuidBuffer = ByteBuffer.wrap(arweaveSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String requestMFP = Hex.toHexString(arweaveSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw new XfpNotMatchException("test", "Master fingerprint not match");
            }
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putInt(BundleKeys.SALT_LEN_KEY, arweaveSignRequest.getSaltLen().getLength());
            String signData = Hex.toHexString(arweaveSignRequest.getSignData());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, signData);
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, arweaveSignRequest.getOrigin());
            ArweaveSignRequest.SignType signType = arweaveSignRequest.getSignType();
            switch (signType) {
                case TRANSACTION:
                    return new Destination(R.id.action_to_arweaveConfirmTransactionFragment, bundle);
                case DATAITEM:
                    throw new InvalidTransactionException("test", "Transaction type DataItem not supported yet");
                default:
                    return new Destination(R.id.action_to_arweaveSignMessageFragment, bundle);
            }
        }
    }

    private static class CosmosSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            CosmosSignRequest cosmosSignRequest = (CosmosSignRequest) object;
            String requestMFP = Hex.toHexString(cosmosSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw XfpNotMatchException.newInstance();
            }
            ByteBuffer uuidBuffer = ByteBuffer.wrap(cosmosSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = cosmosSignRequest.getDerivationPath();
            String signData = Hex.toHexString(cosmosSignRequest.getSignData());

            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, signData);
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, cosmosSignRequest.getOrigin());

            String dataType = cosmosSignRequest.getType().getType();
            if (dataType.equals(CosmosSignRequest.DataType.AMINO.getType()) || dataType.equals(CosmosSignRequest.DataType.DIRECT.getType())) {
                bundle.putString(BundleKeys.DATA_TYPE_KEY, cosmosSignRequest.getType().getType());
                return new Destination(R.id.action_to_cosmosConfirmTransactionFragment, bundle);
            } else if (dataType.equals(CosmosSignRequest.DataType.MESSAGE.getType())) {
                return new Destination(R.id.action_to_cosmosSignMessageFragment, bundle);
            }
            throw UnimplementedException.newInstance();
        }
    }

    private static class EvmSignRequestProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            EvmSignRequest evmSignRequest = (EvmSignRequest) object;
            String requestMFP = Hex.toHexString(evmSignRequest.getMasterFingerprint());
            String MFP = new GetMasterFingerprintCallable().call();
            if (!requestMFP.equalsIgnoreCase(MFP)) {
                throw XfpNotMatchException.newInstance();
            }
            ByteBuffer uuidBuffer = ByteBuffer.wrap(evmSignRequest.getRequestId());
            UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
            String hdPath = evmSignRequest.getDerivationPath();
            String signData = Hex.toHexString(evmSignRequest.getSignData());

            Bundle bundle = new Bundle();
            bundle.putString(BundleKeys.REQUEST_ID_KEY, uuid.toString());
            bundle.putString(BundleKeys.SIGN_DATA_KEY, signData);
            bundle.putString(BundleKeys.HD_PATH_KEY, "M/" + hdPath);
            bundle.putString(BundleKeys.SIGN_ORIGIN_KEY, evmSignRequest.getOrigin());
            String dataType = evmSignRequest.getDataType();
            if (dataType.equals(EvmSignRequest.DataType.AMINO_TRANSACTION.getType())
                    || dataType.equals(EvmSignRequest.DataType.DIRECT_TRANSACTION.getType())) {
                bundle.putString(BundleKeys.DATA_TYPE_KEY, dataType);
                bundle.putLong(BundleKeys.CUSTOM_CHAIN_IDENTIFIER_KEY, evmSignRequest.getCustomChainIdentifier());
                return new Destination(R.id.action_to_cosmosConfirmTransactionFragment, bundle);
            } else {
                throw new InvalidTransactionException("test", "Transaction type ARBITRARY_TRANSACTION not supported yet");
            }
        }
    }

    private static class BytesProcessor implements URResolver {

        @Override
        public Destination run(Object object) throws BaseException {
            String hex = Hex.toHexString((byte[]) object);
            //xrp toolkit tx, metamask tx, webauth tx, decode as JSON
            JSONObject json = tryDecodeAsJson(hex);

            //keystone, try decode as protobuf
            if (json == null) {
                json = tryDecodeAsProtobuf(hex);
            }

            if (json != null) {
                Destination destination = decodeAndProcess(json);
                if (destination == null) {
                    throw new UnknownQrCodeException("test", "unknown UR qr code");
                }
                return destination;
            } else {
                throw new UnknownQrCodeException("test", "unknown UR qr code");
            }
        }

        private JSONObject tryDecodeAsJson(String hex) {
            try {
                return new JSONObject(new String(Hex.decode(hex)));
            } catch (Exception ignored) {
            }
            return null;
        }

        private JSONObject tryDecodeAsProtobuf(String hex) {
            JSONObject object;
            hex = ZipUtil.unzip(hex);
            object = new ProtoParser(Hex.decode(hex)).parseToJson();
            return object;
        }

        private Destination decodeAndProcess(JSONObject object) throws BaseException {
            Destination destination = checkWebAuth(object);
            if (destination != null) return destination;
            if (object.has("TransactionType")) {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.SIGN_DATA_KEY, object.toString());
                return new Destination(R.id.action_to_rippleConfirmTransactionFragment, bundle);
            }
            if (object.optString("type").equals("TYPE_SIGN_TX")) {
                return handleSign(object);
            }
            throw new UnknownQrCodeException("test", "unknown qr code type");
        }

        private Destination checkWebAuth(JSONObject object) {
            try {
                JSONObject webAuth = object.optJSONObject("data");
                if (webAuth != null && webAuth.optString("type").equals("webAuth")) {
                    String data = webAuth.getString("data");
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKeys.WEB_AUTH_DATA_KEY, data);
                    return new Destination(R.id.action_to_webAuthResultFragment, bundle);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Destination handleSign(JSONObject object)
                throws BaseException {
            String xfp = new GetMasterFingerprintCallable().call();
            if (!object.optString("xfp").equals(xfp)) {
                throw new XfpNotMatchException("test", "xft not match");
            }
            try {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKeys.SIGN_DATA_KEY, object.getJSONObject("signTx").toString());
                return new Destination(R.id.action_to_keystoneConfirmTransactionFragment, bundle);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
