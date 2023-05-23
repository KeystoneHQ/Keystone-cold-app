package com.keystone.cold.ui.fragment.main.scan.scanner;

import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.remove_wallet_mode.exceptions.scanner.UnsupportedURException;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;
import com.sparrowwallet.hummingbird.registry.EthNFTItem;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.sparrowwallet.hummingbird.registry.aptos.AptosSignRequest;
import com.sparrowwallet.hummingbird.registry.arweave.ArweaveSignRequest;
import com.sparrowwallet.hummingbird.registry.cardano.CardanoSignRequest;
import com.sparrowwallet.hummingbird.registry.cosmos.CosmosSignRequest;
import com.sparrowwallet.hummingbird.registry.evm.EvmSignRequest;
import com.sparrowwallet.hummingbird.registry.extend.QRHardwareCall;
import com.sparrowwallet.hummingbird.registry.near.NearSignRequest;
import com.sparrowwallet.hummingbird.registry.solana.SolNFTItem;
import com.sparrowwallet.hummingbird.registry.solana.SolSignRequest;
import com.sparrowwallet.hummingbird.registry.sui.SuiSignRequest;

import org.spongycastle.util.encoders.Hex;

import java.util.List;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;

public enum ScanResultTypes {
    PLAIN_TEXT,

    UOS,

    UR_BYTES,
    UR_ETH_SIGN_REQUEST,
    UR_ETH_NFT_ITEM,
    UR_SOL_SIGN_REQUEST,
    UR_SOL_NFT_ITEM,
    UR_NEAR_SIGN_REQUEST,
    UR_CRYPTO_PSBT,
    UR_APTOS_SIGN_REQUEST,
    UR_SUI_SIGN_REQUEST,
    UR_ARWEAVE_SIGN_REQUEST,
    UR_COSMOS_SIGN_REQUEST,
    UR_QR_HARDWARE_CALL,
    UR_CARDANO_SIGN_REQUEST,
    UR_EVM_SIGN_REQUEST;


    public boolean isType(String text) {
        if (this == ScanResultTypes.PLAIN_TEXT) {
            return true;
        }
        return false;
    }

    public boolean isType(UR ur) {
        try {
            Object decodeResult = ur.decodeFromRegistry();
            switch (this) {
                case UR_ETH_SIGN_REQUEST:
                    return decodeResult instanceof EthSignRequest;
                case UR_BYTES:
                    return decodeResult instanceof byte[];
                case UR_ETH_NFT_ITEM:
                    return decodeResult instanceof EthNFTItem;
                case UR_SOL_SIGN_REQUEST:
                    return decodeResult instanceof SolSignRequest;
                case UR_SOL_NFT_ITEM:
                    return decodeResult instanceof SolNFTItem;
                case UR_NEAR_SIGN_REQUEST:
                    return decodeResult instanceof NearSignRequest;
                case UR_APTOS_SIGN_REQUEST:
                    return decodeResult instanceof AptosSignRequest;
                case UR_SUI_SIGN_REQUEST:
                    return decodeResult instanceof SuiSignRequest;
                case UR_CRYPTO_PSBT:
                    return decodeResult instanceof CryptoPSBT;
                case UR_COSMOS_SIGN_REQUEST:
                    return decodeResult instanceof CosmosSignRequest;
                case UR_ARWEAVE_SIGN_REQUEST:
                    return decodeResult instanceof ArweaveSignRequest;
                case UR_EVM_SIGN_REQUEST:
                    return decodeResult instanceof EvmSignRequest;
                case UR_QR_HARDWARE_CALL:
                    return decodeResult instanceof QRHardwareCall;
                case UR_CARDANO_SIGN_REQUEST:
                    return decodeResult instanceof CardanoSignRequest;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isType(SubstratePayload substratePayload) {
        if (this == ScanResultTypes.UOS) {
            return true;
        }
        return false;
    }

    public Object resolveURHex(String hex) {
        try {
            byte[] cborPayload = Hex.decode(hex);
            List<DataItem> items = CborDecoder.decode(cborPayload);
            DataItem dataItem = items.get(0);
            switch (this) {
                case UR_EVM_SIGN_REQUEST:
                    return EvmSignRequest.fromCbor(dataItem);
                case UR_COSMOS_SIGN_REQUEST:
                    return CosmosSignRequest.fromCbor(dataItem);
                case UR_APTOS_SIGN_REQUEST:
                    return AptosSignRequest.fromCbor(dataItem);
                case UR_SUI_SIGN_REQUEST:
                    return SuiSignRequest.fromCbor(dataItem);
                case UR_NEAR_SIGN_REQUEST:
                    return NearSignRequest.fromCbor(dataItem);
                case UR_SOL_NFT_ITEM:
                    return SolNFTItem.fromCbor(dataItem);
                case UR_SOL_SIGN_REQUEST:
                    return SolSignRequest.fromCbor(dataItem);
                case UR_ETH_SIGN_REQUEST:
                    return EthSignRequest.fromCbor(dataItem);
                case UR_ETH_NFT_ITEM:
                    return EthNFTItem.fromCbor(dataItem);
                case UR_CRYPTO_PSBT:
                    return CryptoPSBT.fromCbor(dataItem);
                case UR_ARWEAVE_SIGN_REQUEST:
                    return ArweaveSignRequest.fromCbor(dataItem);
                case UR_BYTES:
                    return ((ByteString) dataItem).getBytes();
                case UR_QR_HARDWARE_CALL:
                    return QRHardwareCall.fromCbor(dataItem);
                case UR_CARDANO_SIGN_REQUEST:
                    return CardanoSignRequest.fromCbor(dataItem);
                default:
                    return null;
            }

        } catch (CborException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ScanResultTypes fromUR(UR ur) throws UnsupportedURException {
        String type = ur.getType();
        switch (type) {
            case "bytes":
                return UR_BYTES;
            case "eth-sign-request":
                return UR_ETH_SIGN_REQUEST;
            case "eth-nft-item":
                return UR_ETH_NFT_ITEM;
            case "sol-sign-request":
                return UR_SOL_SIGN_REQUEST;
            case "sol-nft-item":
                return UR_SOL_NFT_ITEM;
            case "near-sign-request":
                return UR_NEAR_SIGN_REQUEST;
            case "crypto-psbt":
                return UR_CRYPTO_PSBT;
            case "aptos-sign-request":
                return UR_APTOS_SIGN_REQUEST;
            case "sui-sign-request":
                return UR_SUI_SIGN_REQUEST;
            case "arweave-sign-request":
                return UR_ARWEAVE_SIGN_REQUEST;
            case "cosmos-sign-request":
                return UR_COSMOS_SIGN_REQUEST;
            case "evm-sign-request":
                return UR_EVM_SIGN_REQUEST;
            case "qr-hardware-call":
                return UR_QR_HARDWARE_CALL;
            case "cardano-sign-request":
                return UR_CARDANO_SIGN_REQUEST;
            default:
                throw new UnsupportedURException(MainApplication.getApplication().getString(R.string.invalid_qr_code_hint), "unsupported ur type: " + ur.getType());
        }
    }
}
