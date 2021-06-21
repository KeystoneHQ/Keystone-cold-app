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

package com.keystone.coinlib;

import android.text.TextUtils;

import com.keystone.coinlib.exception.InvalidPathException;
import com.keystone.coinlib.path.AddressIndex;
import com.keystone.coinlib.path.Change;
import com.keystone.coinlib.path.CoinPath;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.SignatureDecodeException;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.spec.ECPoint;
import java.util.Arrays;

import static com.keystone.coinlib.coins.BTC.Electrum.TxUtils.int2bytes;

public class Util {

    public static String pubKeyFromExtentPubKey(String extendPubKey) {
        DeterministicKey key = DeterministicKey.deserializeB58(extendPubKey, MainNetParams.get());
        return Hex.toHexString(key.getPubKey());
    }

    public static String getPublicKeyHex(String accountXpub, String hdPath) {
        AddressIndex addressIndex;
        try {
            addressIndex = CoinPath.parsePath(hdPath);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return null;
        }
        Change changeIndex = addressIndex.getParent();
        DeterministicKey account = DeterministicKey.deserializeB58(accountXpub, MainNetParams.get());
        DeterministicKey change = HDKeyDerivation.deriveChildKey(account, changeIndex.getValue());
        return HDKeyDerivation.deriveChildKey(change, addressIndex.getValue()).getPublicKeyAsHex();
    }

    public static String getPublicKeyHex(String accountXpub, int change, int index) {
        DeterministicKey accountKey = DeterministicKey.deserializeB58(accountXpub, MainNetParams.get());
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(accountKey, change);
        return HDKeyDerivation.deriveChildKey(changeKey, index).getPublicKeyAsHex();
    }

    public static String getPublicKeyHex(String exPub) {
        DeterministicKey key = DeterministicKey.deserializeB58(exPub, MainNetParams.get());
        return key.getPublicKeyAsHex();
    }

    /**
     * Keccak-256 hash function.
     *
     * @param input binary encoded input data
     * @param offset of start of data
     * @param length of data
     * @return hash value
     */
    public static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }

    /**
     * Keccak-256 hash function.
     *
     * @param input binary encoded input data
     * @return hash value
     */
    public static byte[] sha3(byte[] input) {
        return sha3(input, 0, input.length);
    }

    /**
     * Keccak-256 hash function that operates on a UTF-8 encoded String.
     *
     * @param utf8String UTF-8 encoded string
     * @return hash value as hex encoded string
     */
    public static String sha3String(String utf8String) {
        return Hex.toHexString(sha3(utf8String.getBytes(StandardCharsets.UTF_8)));
    }

    public static String cleanHexPrefix(String input) {
        if (containsHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    public static String prependHexPrefix(String input) {
        if (!containsHexPrefix(input)) {
            return "0x" + input;
        } else {
            return input;
        }
    }

    public static boolean containsHexPrefix(String input) {
        return !TextUtils.isEmpty(input) && input.length() > 1
                && input.charAt(0) == '0' && input.charAt(1) == 'x';
    }

    public static byte[] trimOrAddLeadingZeros(byte[] bytes) {
        if (bytes.length != 32) {
            while (bytes.length < 32) {
                bytes = concat(new byte[]{0x00}, bytes);
            }
            while (bytes.length > 32) {
                bytes = Arrays.copyOfRange(bytes, bytes.length - 32, bytes.length);
            }
        }
        return bytes;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] decodeRSFromDER(byte[] bytes) {
        try {
            ECKey.ECDSASignature signature = decodeFromDER(bytes);
            return concat(trimOrAddLeadingZeros(signature.r.toByteArray()),
                    trimOrAddLeadingZeros(signature.s.toByteArray()));
        } catch (SignatureDecodeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] extractPublicKey(ECPoint ecPoint) {
        byte[] x = ecPoint.getAffineX().toByteArray();
        byte[] y = ecPoint.getAffineY().toByteArray();
        return Util.concat(Util.trimOrAddLeadingZeros(x), Util.trimOrAddLeadingZeros(y));
    }

    public static ECKey.ECDSASignature decodeFromDER(byte[] bytes) throws SignatureDecodeException {
        ASN1InputStream decoder = null;
        try {
            // BouncyCastle by default is strict about parsing ASN.1 integers. We relax this check, because some
            // Bitcoin signatures would not parse.
            Properties.setThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer", true);
            decoder = new ASN1InputStream(bytes);
            final ASN1Primitive seqObj = decoder.readObject();
            if (seqObj == null)
                throw new SignatureDecodeException("Reached past end of ASN.1 stream.");
            if (!(seqObj instanceof DLSequence))
                throw new SignatureDecodeException("Read unexpected class: " + seqObj.getClass().getName());
            final DLSequence seq = (DLSequence) seqObj;
            ASN1Integer r, s;
            try {
                r = (ASN1Integer) seq.getObjectAt(0);
                s = (ASN1Integer) seq.getObjectAt(1);
            } catch (ClassCastException e) {
                throw new SignatureDecodeException(e);
            }
            // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
            // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
            return new ECKey.ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
        } catch (IOException e) {
            throw new SignatureDecodeException(e);
        } finally {
            if (decoder != null)
                try { decoder.close(); } catch (IOException x) {}
            Properties.removeThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer");
        }
    }

    public static String convertXpubToYpub(String xpub) {
        byte[] bytes = Base58.decodeChecked(xpub);
        byte[] result = new byte[bytes.length + 4];
        System.arraycopy(int2bytes(0x049d7cb2), 0, bytes, 0, 4);
        byte[] checksum = Sha256Hash.hashTwice(bytes, 0, bytes.length);
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        System.arraycopy(checksum, 0, result, bytes.length, 4);
        return Base58.encode(result);
    }
}
