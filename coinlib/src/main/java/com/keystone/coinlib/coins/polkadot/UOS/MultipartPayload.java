package com.keystone.coinlib.coins.polkadot.UOS;

import com.keystone.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.DecoderException;

public class MultipartPayload {
    private final String rawData;

    public int frameCount;
    public boolean isMultiPart;
    public int currentFrame;

    public byte[] frameData;

    public SubstratePayload substratePayload;

    public MultipartPayload(String rawData) throws InvalidUOSException {
        this.rawData = rawData;
        read();
    }

    private void read() throws InvalidUOSException, DecoderException {
        String frameInfo = rawData.substring(0, 10);
        frameCount = Utils.tryParseInt(frameInfo.substring(2, 6));
        isMultiPart = frameCount > 1;
        if(frameCount > 50) {
            throw new InvalidUOSException("Frames number is too big, the QR seems not to be a recognized extrinsic raw data");
        }
        currentFrame = Utils.tryParseInt(frameInfo.substring(6, 10));

        String uosAfterFrame = rawData.substring(10);
        String zerothByte = uosAfterFrame.substring(0, 2);

        if(isMultiPart) {
            frameData = Hex.decode(uosAfterFrame);
            return;
        }

        if ("53".equals(zerothByte)) {
            substratePayload = new SubstratePayload(uosAfterFrame.substring(2));
            return;
        }
        throw new InvalidUOSException("current not support ethereum and legacy ethereum payload");
    }
}
