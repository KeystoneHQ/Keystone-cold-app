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

package com.keystone.cold;

import com.keystone.coinlib.Util;
import com.keystone.cold.protobuf.BaseProtoc;
import com.keystone.cold.protobuf.MessageProtoc;
import com.keystone.cold.protobuf.PayloadProtoc;
import com.keystone.cold.protobuf.SyncProtoc;

import org.junit.Test;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ProtobufTest {

    @Test
    public void testSync() {
        SyncProtoc.Account.Builder zcoinAccount = SyncProtoc.Account.newBuilder()
                .setHdPath("M/44'/136'/0'")
                .setXPub("xpub6CCyKnUGB4VjMLZYGaSvBijULh1Hd2Uimy9EKy5yUQt9Yexb7s24We2CTM54hWaQZYhCzSR6yEFAs5cQ8TwbaSn53S6HRrmaFkdgqczb85v")
                .setAddressLength(1)
                .setIsMultiSign(false);

        SyncProtoc.Coin.Builder zcoin = SyncProtoc.Coin.newBuilder()
                .setCoinCode("ZCOIN")
                .setActive(true)
                .addAccounts(zcoinAccount);


        SyncProtoc.Account.Builder dashAccount = SyncProtoc.Account.newBuilder()
                .setHdPath("M/44'/5'/0'")
                .setXPub("xpub6CYEjsU6zPM3sADS2ubu2aZeGxCm3C5KabkCpo4rkNbXGAH9M7rRUJ4E5CKiyUddmRzrSCopPzisTBrXkfCD4o577XKM9mzyZtP1Xdbizyk")
                .setAddressLength(1)
                .setIsMultiSign(false);

        SyncProtoc.Coin.Builder dash = SyncProtoc.Coin.newBuilder()
                .setCoinCode("DASH")
                .setActive(true)
                .addAccounts(dashAccount);

        SyncProtoc.Sync.Builder sync = SyncProtoc.Sync.newBuilder()
                .addCoins(dash)
                .addCoins(zcoin);


        PayloadProtoc.Payload.Builder payload = PayloadProtoc.Payload.newBuilder()
                .setType(PayloadProtoc.Payload.Type.TYPE_SYNC)
                .setXfp("464ce7dec19b055796f5a686630d477b06ed1eb1ad579d237741271a5729e241d1bd7e0f613579dcbbd298241551aeb3196c26f085f33d191a55da2ef6fc65d0e7cbe7eec2175add221e758cfa931eb6")
                .setSync(sync);

        BaseProtoc.Base.Builder base = BaseProtoc.Base.newBuilder()
                .setData(payload)
                .setVersion(1)
                .setDescription("keystone valut qrcode protocol")
                .setColdVersion(10226);

        byte[] data = base.build().toByteArray();
        assertEquals("CAESGmNvYm8gdmFsdXQgcXJjb2RlIHByb3RvY29sGscDCAESoAE0NjRjZTdkZWMxOWIwNTU3OTZmNWE2ODY2MzBkNDc3YjA2ZWQxZWIxYWQ1NzlkMjM3NzQxMjcxYTU3MjllMjQxZDFiZDdlMGY2MTM1NzlkY2JiZDI5ODI0MTU1MWFlYjMxOTZjMjZmMDg1ZjMzZDE5MWE1NWRhMmVmNmZjNjVkMGU3Y2JlN2VlYzIxNzVhZGQyMjFlNzU4Y2ZhOTMxZWI2Gp8CCosBCgREQVNIEAEagAEKC00vNDQnLzUnLzAnEm94cHViNkNZRWpzVTZ6UE0zc0FEUzJ1YnUyYVplR3hDbTNDNUthYmtDcG80cmtOYlhHQUg5TTdyUlVKNEU1Q0tpeVVkZG1SenJTQ29wUHppc1RCclhrZkNENG81NzdYS005bXp5WnRQMVhkYml6eWsYAQqOAQoFWkNPSU4QARqCAQoNTS80NCcvMTM2Jy8wJxJveHB1YjZDQ3lLblVHQjRWak1MWllHYVN2QmlqVUxoMUhkMlVpbXk5RUt5NXlVUXQ5WWV4YjdzMjRXZTJDVE01NGhXYVFaWWhDelNSNnlFRkFzNWNROFR3YmFTbjUzUzZIUnJtYUZrZGdxY3piODV2GAEo8k8=", Base64.toBase64String(data));

    }

    @Test
    public void testMessage() {
        MessageProtoc.SignMessage.Builder builder = MessageProtoc.SignMessage.newBuilder()
                .setCoinCode("BTC").setHdPath("M/49'/0/'0'/0/0").setMessage("hello");

        PayloadProtoc.Payload.Builder payload = PayloadProtoc.Payload.newBuilder()
                .setType(PayloadProtoc.Payload.Type.TYPE_SIGN_MSG)
                .setXfp("464ce7dec19b055796f5a686630d477b06ed1eb1ad579d237741271a5729e241d1bd7e0f613579dcbbd298241551aeb3196c26f085f33d191a55da2ef6fc65d0e7cbe7eec2175add221e758cfa931eb6")
                .setSignMsg(builder);

        BaseProtoc.Base.Builder base = BaseProtoc.Base.newBuilder()
                .setData(payload)
                .setVersion(1)
                .setDescription("keystone valut qrcode protocol")
                .setColdVersion(10226);

        assertEquals("CAESGmNvYm8gdmFsdXQgcXJjb2RlIHByb3RvY29sGsQBCAMSoAE0NjRjZTdkZWMxOWIwNTU3OTZmNWE2ODY2MzBkNDc3YjA2ZWQxZWIxYWQ1NzlkMjM3NzQxMjcxYTU3MjllMjQxZDFiZDdlMGY2MTM1NzlkY2JiZDI5ODI0MTU1MWFlYjMxOTZjMjZmMDg1ZjMzZDE5MWE1NWRhMmVmNmZjNjVkMGU3Y2JlN2VlYzIxNzVhZGQyMjFlNzU4Y2ZhOTMxZWI2Kh0KA0JUQxIPTS80OScvMC8nMCcvMC8wGgVoZWxsbyjyTw==",
                Base64.toBase64String(base.build().toByteArray()));

    }

    public static void main(String[] args) throws Exception {
        /*
         * Generate an ECDSA signature
         */

        /*
         * Generate a key pair
         */

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");

        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

        KeyPair pair = keyGen.generateKeyPair();
        ECPrivateKey priv = (ECPrivateKey) pair.getPrivate();
        System.out.println("priv der: "+ Hex.toHexString(priv.getEncoded()));
        System.out.println("priv hex: " + priv.getS().toString(16));

        ECPublicKey pub = (ECPublicKey) pair.getPublic();

        ;
        System.out.println("pub hex:" + Hex.toHexString( Util.extractPublicKey(pub.getW())));
        System.out.println("pub der: " + Hex.toHexString(pub.getEncoded()));

        /*
         * Create a Signature object and initialize it with the private key
         */

        Signature ecdsa = Signature.getInstance("SHA256withECDSA");

        ecdsa.initSign(priv);

        String str = "This is string to sign";
        byte[] strByte = str.getBytes("UTF-8");
        ecdsa.update(strByte);

        /*
         * Now that all the data to be signed has been read in, generate a
         * signature for it
         */

        byte[] realSig = ecdsa.sign();
        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));

    }

    public static byte[] encrypt( byte[] content, byte[] publicKey ) throws Exception {
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(content);
    }

    public static byte[] decrypt(byte[] content, byte[] privateKey) throws Exception{
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return cipher.doFinal(content);
    }


}
