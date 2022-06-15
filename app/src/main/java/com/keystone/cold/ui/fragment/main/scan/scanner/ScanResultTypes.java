package com.keystone.cold.ui.fragment.main.scan.scanner;

import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.EthNFTItem;
import com.sparrowwallet.hummingbird.registry.EthSignRequest;
import com.sparrowwallet.hummingbird.registry.solana.SolNFTItem;
import com.sparrowwallet.hummingbird.registry.solana.SolSignRequest;

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
    UR_SOL_NFT_ITEM;


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
                case UR_SOL_NFT_ITEM:
                    return SolNFTItem.fromCbor(dataItem);
                case UR_SOL_SIGN_REQUEST:
                    return SolSignRequest.fromCbor(dataItem);
                case UR_ETH_SIGN_REQUEST:
                    return EthSignRequest.fromCbor(dataItem);
                case UR_ETH_NFT_ITEM:
                    return EthNFTItem.fromCbor(dataItem);
                case UR_BYTES:
                    return ((ByteString) dataItem).getBytes();
                default:
                    return null;
            }

        } catch (CborException e) {
            e.printStackTrace();
            return null;
        }
    }
}
