/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.polkadot;

import android.text.TextUtils;
import android.util.Log;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class TransactionEncoder {
    private final ChainProperty chainProperty;
    private final String from;
    public long amount;
    public String dest;
    public String blockHash;
    public long nonce;
    public long tip;
    public long transactionVersion;
    public long specVersion;
    public long validityPeriod;
    public int blockNumber;
    private String signature;

    TransactionEncoder(ChainProperty chainProperty,
                       long amount,
                       String dest,
                       String blockHash,
                       long nonce,
                       long tip,
                       long transactionVersion,
                       long specVersion,
                       long validityPeriod,
                       int blockNumber,
                       String from) {
        this.chainProperty = chainProperty;
        this.amount = amount;
        this.dest = dest;
        this.blockHash = blockHash;
        this.nonce = nonce;
        this.tip = tip;
        this.transactionVersion = transactionVersion;
        this.specVersion = specVersion;
        this.validityPeriod = validityPeriod;
        this.blockNumber = blockNumber;
        this.from = from;
    }

    public static byte[] int16ToBytes(int i) {
        return new byte[]{
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    public void addSignature(String signature) {
        this.signature = signature;
    }

    public byte[] encode() {
        try {
            if (TextUtils.isEmpty(signature)) {
                return constructTransaction();
            } else {
                return constructSignedTransaction();
            }
        } catch (Exception ignored) {
            Log.w("TransactionEncoder", "encode exception" ,ignored);
        }
        return null;
    }

    public byte[] constructTransaction() throws Exception {
        ScaleCodecWriter codecWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        codecWriter.writeByteArray(int16ToBytes(chainProperty.callId.transfer));
        byte[] publicKey = AddressCodec.decodeAddress(dest);
        codecWriter.writeByte(00);
        codecWriter.writeByteArray(publicKey);
        codecWriter.writeBIntCompact(BigInteger.valueOf(amount));
        byte[] mortalEra = constructEra(blockNumber, validityPeriod);
        codecWriter.writeByteArray(mortalEra);
        codecWriter.writeLIntCompact(nonce);
        codecWriter.writeLIntCompact(tip);
        codecWriter.writeUint32(specVersion);
        codecWriter.writeUint32(transactionVersion);
        codecWriter.writeByteArray(Hex.decode(chainProperty.genesisHash));
        codecWriter.writeByteArray(Hex.decode(blockHash.replace("0x","")));
        return codecWriter.toByteArray();
    }

    public byte[] constructSignedTransaction() throws Exception {
        ScaleCodecWriter scaleCodecWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        scaleCodecWriter.writeByte(chainProperty.payloadVersion);
        scaleCodecWriter.writeByte(00);
        scaleCodecWriter.writeByteArray(AddressCodec.decodeAddress(from));
        scaleCodecWriter.writeByte(0x01);
        scaleCodecWriter.writeByteArray(Hex.decode(signature));
        byte[] mortalEra = constructEra(blockNumber, validityPeriod);
        scaleCodecWriter.writeByteArray(mortalEra);
        scaleCodecWriter.writeLIntCompact(nonce);
        scaleCodecWriter.writeLIntCompact(tip);
        scaleCodecWriter.writeByteArray(int16ToBytes(chainProperty.callId.transfer));
        byte[] destPublicKey = AddressCodec.decodeAddress(dest);
        scaleCodecWriter.writeByte(00);
        scaleCodecWriter.writeByteArray(destPublicKey);
        scaleCodecWriter.writeBIntCompact(BigInteger.valueOf(amount));
        byte[] txContent = scaleCodecWriter.toByteArray();
        ScaleCodecWriter finalWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        finalWriter.writeCompact(txContent.length);
        finalWriter.writeByteArray(txContent);
        return finalWriter.toByteArray();
    }

    private byte[] constructEra(int blockNumber, long eraPeriod) {
        int calPeriod = (int) Math.pow(2, Math.ceil(Math.log(eraPeriod) / Math.log(2)));
        calPeriod = Math.min(Math.max(calPeriod, 4), 1 << 16);
        int phase = blockNumber % calPeriod;
        int quantizeFactor = Math.max(calPeriod >> 12, 1);
        int quantizedPhase = phase / quantizeFactor * quantizeFactor;
        int trailingZeros = getTrailingZeros(eraPeriod);
        int encoded = Math.min(15, Math.max(1, trailingZeros - 1)) + (((quantizedPhase / quantizeFactor) << 4));
        byte first = (byte) (encoded >> 8);
        byte second = (byte) ((byte) encoded & 0xff);
        return new byte[]{second, first};
    }

    private int getTrailingZeros(Long period) {
        String binary = Long.toString(period, 2);
        int index = 0;
        while (binary.toCharArray()[binary.length() - 1 - index] == '0') {
            index++;
        }
        return index;
    }
}
