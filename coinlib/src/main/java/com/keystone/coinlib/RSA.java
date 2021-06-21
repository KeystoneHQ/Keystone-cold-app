package com.keystone.coinlib;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static com.keystone.coinlib.PemUtil.readPrivateKeyFromPem;
import static com.keystone.coinlib.PemUtil.readPublicKeyFromPem;

public class RSA {
    static String ALGORITHM = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    public static byte[] encrypt(byte[] content, String pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        PublicKey pubkey = readPublicKeyFromPem(pubKey);
        cipher.init(Cipher.ENCRYPT_MODE, pubkey,
                new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        return cipher.doFinal(content);
    }

    public static byte[] decrypt(byte[] content, String privateKey)  throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        PrivateKey privKey = readPrivateKeyFromPem(privateKey);
        cipher.init(Cipher.DECRYPT_MODE, privKey,
                new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        return cipher.doFinal(content);
    }
}
