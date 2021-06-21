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

import com.keystone.coinlib.Util;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TxUtils {

    private static final NetworkParameters MAINNET = MainNetParams.get();

    public static byte[] int2bytes(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }

    public static PubKeyInfo getPubKeyInfo(String xPubkey) throws ElectrumTx.SerializationException {
        String type = xPubkey.substring(0,2);
        if ("ff".equals(type)) {
            int stringLength = xPubkey.length();
            String trimmedPubKey = xPubkey.substring(2, stringLength);
            return parseXpubKey(trimmedPubKey);
        } else {
            throw new ElectrumTx.SerializationException("currently only support bip32 extended type");
        }
    }

    public static PubKeyInfo parseXpubKey(String pubkey) throws ElectrumTx.SerializationException {
        byte[] pubKeyRaw = Hex.decode(pubkey);
        byte[] pubKeyByte = Arrays.copyOfRange(pubKeyRaw, 0, 78);

        byte[] addressBytes = new byte[pubKeyByte.length + 4];
        int p2pkhPub = MAINNET.getBip32HeaderP2PKHpub();
        byte[] p2pkhPubBytes = int2bytes(p2pkhPub);

        System.arraycopy(p2pkhPubBytes, 0, addressBytes, 0, 4);
        System.arraycopy(pubKeyByte, 4, addressBytes, 4, pubKeyByte.length - 4);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, pubKeyByte.length);
        System.arraycopy(checksum, 0, addressBytes, pubKeyByte.length, 4);
        String xPubKey = Base58.encode(addressBytes);


        byte[] dd = Arrays.copyOfRange(pubKeyRaw, 78, pubKeyRaw.length);
        ByteArrayInputStream a = new ByteArrayInputStream(dd);
        List<Long> s = new ArrayList<Long>();
        while (a.available() > 0) {
            byte[] temp = new byte[2];
            a.read(temp, 0, 2);
            long n = Utils.readUint16(temp, 0);
            if( n == 0xffff) {
                byte[] temp2 = new byte[4];
                a.read(temp2, 0, 4);
                 n = Utils.readUint32(temp2, 0);
            }
            s.add(n);
        };

        return new PubKeyInfo(xPubKey, s);

    }

    public static boolean isMasterPublicKeyMatch(String xpub, ElectrumTx tx) {
        return tx.getInputs()
                .stream()
                .allMatch(input -> xpub.equals(input.pubKey.xpub));
    }

    public static class PubKeyInfo {
        public String xpub;
        public List<Long> levels;
        public String pubkey;
        public String hdPath;

        public PubKeyInfo(String xpub, List<Long> levels) throws ElectrumTx.SerializationException {
            this.xpub = xpub;
            this.levels = levels;
            // since bitcoinj current version not support ypub,
            // current we only support p2sh-p2wpkh address type,
            // we use 49 index now
            if(xpub.startsWith("ypub") || xpub.startsWith("xpub")) {
                this.hdPath = String.format(Locale.US,"M/49'/0'/0'/%d/%d", this.levels.get(0), this.levels.get(1));
            }  else if(xpub.startsWith("zpub")) {
                this.hdPath = String.format(Locale.US,"M/84'/0'/0'/%d/%d", this.levels.get(0), this.levels.get(1));
            } else {
                throw new ElectrumTx.SerializationException("extended key type is not supported");
            }
            this.pubkey = Util.getPublicKeyHex(this.xpub, this.hdPath);
        }
    }
}

