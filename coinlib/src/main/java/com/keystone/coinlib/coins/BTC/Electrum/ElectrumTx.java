/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.coins.BTC.Electrum;

import androidx.annotation.NonNull;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;


public class ElectrumTx {
    private long version;
    private boolean partial;
    private boolean isSegwit;
    private long lockTime;

    private ArrayList<TransactionInput> inputs;
    private ArrayList<TransactionOutput> outputs;

    private static final String PARTIAL_TXN_HEADER_MAGIC = "45505446ff";
    private static final int PARTIAL_FORMAT_VERSION = 0;

    public ElectrumTx(long version, boolean partial, boolean isSegwit){
        this.version = version;
        this.partial = partial;
        this.isSegwit = isSegwit;
    }

    public static ElectrumTx parse(byte[] rawElectrumTx) throws SerializationException {
        int offset = 0;
        int length = rawElectrumTx.length;
        ByteArrayInputStream rawTxStream = new ByteArrayInputStream(rawElectrumTx);
        byte[] magicPrefix = new byte[5];
        offset += rawTxStream.read(magicPrefix, 0, 5);
        boolean partial = PARTIAL_TXN_HEADER_MAGIC.equals(Hex.toHexString(magicPrefix));
        byte[] formatVersion = new byte[1];
        offset += rawTxStream.read(formatVersion, 0, 1);


        if (formatVersion[0] != PARTIAL_FORMAT_VERSION) {
            throw new SerializationException("format version is not supported");
        }

        int txLength = length - offset;

        byte[] txPlayLoad = new byte[txLength];

        rawTxStream.read(txPlayLoad, 0, txLength);

        Message txMessage = new Message(txPlayLoad);
        long version = txMessage.readUint32();
        byte marker = txMessage.payload[txMessage.cursor];
        boolean useSegwit = marker == 0;
        if (useSegwit) {
            txMessage.readBytes(2);
        }
        ElectrumTx tx = new ElectrumTx(version, partial, useSegwit);
        tx.parseInput(txMessage);
        tx.parseOutput(txMessage);

        for(int i =0; i < tx.inputs.size(); i++) {
            parseWitness(txMessage, tx.inputs.get(i));
        }
        tx.lockTime = txMessage.readUint32();
        return tx;
    }

    public long getVersion() {
        return version;
    }

    public boolean isPartial() {
        return partial;
    }

    public boolean isSegwit() {
        return isSegwit;
    }


    public long getLockTime() {
        return lockTime;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    private void parseInput(Message txMessage) throws SerializationException {
        long numInputs = txMessage.readVarInt();
        inputs = new ArrayList<>(Math.min((int) numInputs, Utils.MAX_INITIAL_ARRAY_LENGTH));
        for (long i = 0; i < numInputs; i++) {
            TransactionInput input = new TransactionInput(txMessage);
            inputs.add(input);
        }
    }

    private void parseOutput(Message txMessage) {
        long numOutputs = txMessage.readVarInt();
        outputs = new ArrayList<>(Math.min((int) numOutputs, Utils.MAX_INITIAL_ARRAY_LENGTH));
        for (long i = 0; i < numOutputs; i++) {
            TransactionOutput output = new TransactionOutput(txMessage);
            outputs.add(output);
        }
    }

    private static void parseWitness(Message txMessage, TransactionInput txIn) throws SerializationException {
        long n = txMessage.readVarInt();
        if (n == 0) {
            txIn.witness = "00";
        }

        if (n == 4294967295L){
            txIn.value = txMessage.readUint64();
            txIn.witnessVersion = txMessage.readUint16();

        }
        long witnessNum = txMessage.readVarInt();
        String[] witnessList = new String[(int) witnessNum];
        for (int i =0; i < witnessNum; i++) {
            witnessList[i] = Hex.toHexString(txMessage.readByteArray());
        }
        txIn.pubKey = TxUtils.getPubKeyInfo(witnessList[1]);
    }

    @NonNull
    @Override
    public String toString() {
        try {
            return "ElectrumTx{" +
                    "version=" + version +
                    ", partial=" + partial +
                    ", isSegwit=" + isSegwit +
                    ", lockTime=" + lockTime +
                    ", inputs=" + printInputs() +
                    ", outputs=" + printOutputs() +
                    '}';
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String printInputs() throws JSONException {
        JSONArray object = new JSONArray();
        for (TransactionInput input : inputs) {
            JSONObject inputObject = new JSONObject();
            inputObject.put("txIndex",input.preTxIndex);
            inputObject.put("txId",input.preTxId);
            inputObject.put("value",input.value);
            inputObject.put("hdPath",input.pubKey.hdPath);
            inputObject.put("pubkey",input.pubKey.pubkey);
            object.put(inputObject);
        }

        return object.toString(4);
    }

    private String printOutputs() throws JSONException {
        JSONArray object = new JSONArray();
        for (TransactionOutput output : outputs) {
            JSONObject outputObject = new JSONObject();
            outputObject.put("address",output.address);
            outputObject.put("value",output.value);
            object.put(outputObject);
        }

        return object.toString(4);
    }

    public static class SerializationException extends Exception {
        SerializationException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static boolean isFinal(@NonNull String hex) {
        byte[] raw = Hex.decode(hex);
        return new Transaction(MainNetParams.get(), raw)
               .getInputs()
               .stream()
               .noneMatch(input -> input.getSequenceNumber() < 0xffffffffL - 1);
    }
}
