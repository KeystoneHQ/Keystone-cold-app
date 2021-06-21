package com.keystone.coinlib.coins.polkadot.UOS;

public class UosDecodeResult {
    public int frameCount;
    public int currentFrame;
    public boolean isMultiPart;
    public boolean isComplete;
    public byte[] frameData;
    private SubstratePayload substratePayload;

    public void setSubstratePayload(SubstratePayload sp) {
        this.substratePayload = sp;
    }

    public SubstratePayload getSubstratePayload() {
        return substratePayload;
    }
}
