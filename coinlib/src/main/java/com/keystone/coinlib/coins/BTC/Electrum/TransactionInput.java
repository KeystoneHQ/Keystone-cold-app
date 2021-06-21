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

import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class TransactionInput {

    private final Message txMessage;
    public String type;
    public String preTxId;
    public long preTxIndex;
    public String scriptSig;
    public long  sequence;
    public String witness;
    public BigInteger value;
    public long witnessVersion;
    public TxUtils.PubKeyInfo pubKey;

    public TransactionInput(Message txMessage) throws ElectrumTx.SerializationException {
        this.txMessage = txMessage;
        this.preTxId = parseTxId();
        this.preTxIndex = parseTxIndex();
        this.scriptSig = parseScriptSig();
        this.sequence = parseSequence();
        this.type = parseType();
    }

    private String parseTxId() {
        return Hex.toHexString(Utils.reverseBytes(this.txMessage.readBytes(32)));
    }

    private long parseTxIndex(){
        return this.txMessage.readUint32();
    }

    private String parseScriptSig(){
        return Hex.toHexString(this.txMessage.readByteArray());
    }

    private long parseSequence(){
        return this.txMessage.readUint32();
    }

    private String parseType() throws ElectrumTx.SerializationException {
        Script script = new Script(Hex.decode(this.scriptSig));
        if(script.getChunks().size() == 1) {
            ScriptChunk chunk = script.getChunks().get(0);
            assert chunk.data != null;
            if(chunk.data.length == 22) {
                return "p2wpkh-p2sh";
            }
            if (chunk.data.length == 34) {
                return "p2wsh-p2sh";
            }
            throw new ElectrumTx.SerializationException("input type is not supported");
        } else {
            throw new ElectrumTx.SerializationException("input type is not supported");
        }
    }
}
