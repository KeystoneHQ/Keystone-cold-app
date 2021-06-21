package com.keystone.coinlib.coins.polkadot.UOS;

import com.keystone.coinlib.Util;
import com.keystone.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

public class UOSDecoder {

    public Map<Integer, UosDecodeResult> results = new HashMap<>();
    private int frameCount = -1;

    public UosDecodeResult decode(String rawData)
            throws InvalidUOSException {
        String UOSRawData = extractUOSRawData(rawData);
        MultipartPayload mp = new MultipartPayload(UOSRawData);
        SubstratePayload sp = mp.substratePayload;

        UosDecodeResult result = new UosDecodeResult();
        result.frameCount = mp.frameCount;
        result.currentFrame = mp.currentFrame;
        result.isMultiPart = mp.isMultiPart;
        result.frameData = mp.frameData;
        if (result.isMultiPart) {
            if (frameCount == -1) {
                frameCount = result.frameCount;
            }
            if (!results.containsKey(result.currentFrame)) {
                results.put(result.currentFrame, result);
                if (results.size() == frameCount) {
                    UosDecodeResult completeResult = combine();
                    completeResult.isComplete = true;
                    return completeResult;
                }
            }
            result.isComplete = false;
        } else {
            result.isComplete = true;
            result.setSubstratePayload(sp);
        }
        return result;
    }

    private UosDecodeResult combine() throws InvalidUOSException, DecoderException {
        byte[] data = new byte[0];
        for (int i = 0; i < frameCount; i++) {
            data = Util.concat(data, results.get(i).frameData);
        }
        UosDecodeResult completeResult = new UosDecodeResult();
        completeResult.frameData = data;
        completeResult.isComplete = true;
        if (data[0] == 0x53) {
            SubstratePayload sp = new SubstratePayload(Hex.toHexString(data).substring(2));
            completeResult.setSubstratePayload(sp);
            return completeResult;
        }
        throw new InvalidUOSException("invalid payload type:" + data[0]);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getScanedFrames() {
        return results.size();
    }

    private static String extractUOSRawData(String QRRawData) throws InvalidUOSException {
        if (QRRawData.length() == 0) {
            throw new InvalidUOSException("QRCode raw data is none");
        }
        if (QRRawData.endsWith("ec")) {
            QRRawData = QRRawData.substring(0, QRRawData.length() - 2);
        }
        while (QRRawData.endsWith("ec11")) {
            QRRawData = QRRawData.substring(0, QRRawData.length() - 4);
        }
        if (!QRRawData.startsWith("4") || !QRRawData.endsWith("0")) {
            throw new InvalidUOSException("QRCode raw data is invalid");
        }
        QRRawData = QRRawData.substring(1, QRRawData.length() - 1);
        int length8 = Utils.tryParseInt(QRRawData.substring(0, 2));
        int length16 = Utils.tryParseInt(QRRawData.substring(0, 4));
        if (length8 * 2 + 2 == QRRawData.length()) {
            QRRawData = QRRawData.substring(2);
        } else if (length16 * 2 + 4 == QRRawData.length()) {
            QRRawData = QRRawData.substring(4);
        } else {
            throw new InvalidUOSException("QRCode raw data is invalid");
        }
        return QRRawData;
    }
}
