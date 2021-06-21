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

public class TransactionEncoderBuilder {
    private ChainProperty chainProperty;
    private long amount;
    private String dest;
    private String blockHash;
    private long nonce;
    private long tip = 0;
    private long transactionVersion;
    private long specVersion;
    private long validityPeriod;
    private int blockNumber;
    private String from;

    public TransactionEncoderBuilder setChainProperty(ChainProperty chainProperty) {
        this.chainProperty = chainProperty;
        return this;
    }

    public TransactionEncoderBuilder setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    public TransactionEncoderBuilder setDest(String dest) {
        this.dest = dest;
        return this;
    }

    public TransactionEncoderBuilder setBlockHash(String blockHash) {
        this.blockHash = blockHash;
        return this;
    }

    public TransactionEncoderBuilder setNonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public TransactionEncoderBuilder setTip(long tip) {
        this.tip = tip;
        return this;
    }

    public TransactionEncoderBuilder setTransactionVersion(long transactionVersion) {
        this.transactionVersion = transactionVersion;
        return this;
    }

    public TransactionEncoderBuilder setSpecVersion(long specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    public TransactionEncoderBuilder setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
        return this;
    }

    public TransactionEncoderBuilder setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    public TransactionEncoderBuilder setFrom(String from) {
        this.from = from;
        return this;
    }

    public TransactionEncoder createSubstrateTransactionInfo() {
        return new TransactionEncoder(chainProperty, amount, dest, blockHash, nonce, tip, transactionVersion, specVersion, validityPeriod, blockNumber, from);
    }
}