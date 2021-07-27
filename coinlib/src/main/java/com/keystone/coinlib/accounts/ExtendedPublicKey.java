package com.keystone.coinlib.accounts;

import com.keystone.coinlib.utils.B58;

import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExtendedPublicKey {
    private final byte[] version;
    private final byte depth;
    private final byte[] parentFingerprint;
    private final byte[] index;
    private final byte[] chainCode;
    private final byte[] key;

    public ExtendedPublicKey(String xPub) {
        byte[] bytes = new B58().decodeAndCheck(xPub);
        this.version = Arrays.copyOfRange(bytes, 0, 4);
        this.depth = bytes[4];
        this.parentFingerprint = Arrays.copyOfRange(bytes, 5, 9);
        this.index = Arrays.copyOfRange(bytes, 9, 13);
        this.chainCode = Arrays.copyOfRange(bytes, 13, 45);
        this.key = Arrays.copyOfRange(bytes, 45, 78);
    }

    public ExtendedPublicKey(byte[] version, byte depth, byte[] parentFingerprint, byte[] index, byte[] chainCode, byte[] key) {
        this.version = version;
        this.depth = depth;
        this.parentFingerprint = parentFingerprint;
        this.index = index;
        this.chainCode = chainCode;
        this.key = key;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte getDepth() {
        return depth;
    }

    public byte[] getParentFingerprint() {
        return parentFingerprint;
    }

    public byte[] getIndex() {
        return index;
    }

    public byte[] getChainCode() {
        return chainCode;
    }

    public byte[] getKey() {
        return key;
    }

    public int getIndexValue() {
        return Integer.valueOf(Hex.toHexString(this.index), 16);
    }

    public String toString() {
        byte[] bytes = new byte[78];
        System.arraycopy(this.version, 0, bytes, 0, 4);
        bytes[4] = this.depth;
        System.arraycopy(this.parentFingerprint, 0, bytes, 5, 4);
        System.arraycopy(this.index, 0, bytes, 9, 13);
        System.arraycopy(this.chainCode, 0, bytes, 13, 32);
        System.arraycopy(this.key, 0, bytes, 45, 33);
        return new String(new B58().encodeToBytesChecked(bytes), StandardCharsets.US_ASCII);
    }
}