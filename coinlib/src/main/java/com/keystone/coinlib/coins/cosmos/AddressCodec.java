package com.keystone.coinlib.coins.cosmos;

import static com.keystone.coinlib.Util.cleanHexPrefix;
import static com.keystone.coinlib.Util.sha3String;

import org.bitcoinj.core.Utils;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressCodec {

    public static final String SEPARATOR  = "@";

    public static String encodeGeneralAddress(String prefix, byte[] publicKey) {
        byte[] hashResult = Utils.sha256hash160(publicKey);
        return Bech32.bech32Encode(prefix.getBytes(StandardCharsets.UTF_8), hashResult);
    }

    public static String encodeEvmosAddress(String prefix, byte[] publicKey) {
        byte[] data = getKeccakHashRight160(publicKey);
        String bech32Address = Bech32.bech32Encode(prefix.getBytes(StandardCharsets.UTF_8), data);
        String ethAddress = toChecksumAddress(Hex.toHexString(data));
        return bech32Address + SEPARATOR + ethAddress;
    }

    private static byte[] getKeccakHashRight160(byte[] publicKey) {
        byte[] hash = new Keccak.Digest256().digest(publicKey);
        return Arrays.copyOfRange(hash, hash.length - 20, hash.length);  // right most 160 bits
    }

    private static String toChecksumAddress(String address) {
        if (hasUpperCase(address)) {
            return address;
        }
        String lowercaseAddress = cleanHexPrefix(address).toLowerCase();
        String addressHash = cleanHexPrefix(sha3String(lowercaseAddress));

        StringBuilder result = new StringBuilder(lowercaseAddress.length() + 2);

        result.append("0x");

        for (int i = 0; i < lowercaseAddress.length(); i++) {
            if (Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) >= 8) {
                result.append(String.valueOf(lowercaseAddress.charAt(i)).toUpperCase());
            } else {
                result.append(lowercaseAddress.charAt(i));
            }
        }
        return result.toString();
    }

    private static boolean hasUpperCase(String address) {
        String regex = ".*[A-F]+.*";
        Matcher matcher = Pattern.compile(regex).matcher(address);
        return matcher.matches();
    }
}