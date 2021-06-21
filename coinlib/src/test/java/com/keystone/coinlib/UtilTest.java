
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

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import static com.keystone.coinlib.RSA.decrypt;
import static com.keystone.coinlib.Util.decodeRSFromDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilTest {
    @Test
    public void testXpub2Pub() {
        String xpub = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        String pub = Util.pubKeyFromExtentPubKey(xpub);
        assertEquals(pub,"0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b8");
    }

    @Test
    public void testXpub2Pub2() {
        String xpub = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        String pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/0");
        assertEquals(pub,"02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67");

        pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/10");
        assertEquals(pub,"02a18c6e271a995b162348b4332b63f13bf031617192d6232e809210ad5d85c382");

        pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/2147483647");
        assertEquals(pub,"032657f51bf6bd3e510e67d5c39235a077d69ac3adac0c260aa471d64bc4dad762");
    }

    @Test
    public void leadingZeros() {
        byte[] bytes = Util.trimOrAddLeadingZeros(new byte[]{0x01});
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));

        bytes = Util.trimOrAddLeadingZeros(Hex.decode("000000000000000000000000000000000000000000000000000000000000000001"));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));

        bytes = Util.trimOrAddLeadingZeros(Hex.decode("0000000000000000000000000000000000000000000000000000000001"));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));
    }
    @Test
    public void testXpubToYpub() {
        assertEquals("ypub6XsyMmCyC7o9aXNfXzxwFgz3XPub9HadNzaZraotUtYjRHkJR7YXvaPmdZvvxhrYh9ajWXBJaPNjPsEPo3M4uNG9LyrrPTaYuee44qgWJW3",
                Util.convertXpubToYpub("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb"));
    }

    @Test
    public void test() {
        byte[] rs = decodeRSFromDER(Hex.decode("3046022100bd871fd1231747a4f96e2b391e538b41988c35d68490938a8b7145136e763d6e022100cebc63388cbbecca658e825e9de0832b1f0286bd29240d34db0505cc80697d13"));
        System.out.println(Hex.toHexString(rs));
    }

    @Test
    public void testRSA() throws Exception {
        String priv = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIJKQIBAAKCAgEAqcta004a1w61ZwL5Au+1rDEiipSLtMKWMkkHWpjbOfkcGOeT\n" +
                "b1fSUlIBssNFBfdmSF1479b/55Zk5d++7ttzbY4mkcLe8bfSEMf0Ke4KJP7po3TT\n" +
                "iH9VuhFd25yeVPdTeVK/pdbuUaYmHlqWtIaYxoWHJ2U3b4rLp2BjpDTiNGq4KYFJ\n" +
                "VKP/rNt4RH6ZwQrVUCvTTWpKrhqnlFWMUK/93RkbCoU0W9nrMTJEe9bS64zv1Byu\n" +
                "/zX8L+kZtR5h/Zo7TvWMBCLKNKWSHZmuhKq8BO1WIq/EFcsybLLw0pRsEgleHzh4\n" +
                "kr6b3GE3LcYSizkAY7YyakGvgP72n22oOwTszE2eIIh3UiJRYg2zr2IlGXxc0sgx\n" +
                "xEb92e2d7zqUP2UsaC+iKG7E2mrmRK/s7tRBVI4Sez9tquKWtOo1/p49fCgnDM2F\n" +
                "Nt1zu2YiH9Lr0pD9mPnEOj+2PIwql432JyaJ4WVazwYFCMuHEr/lzNBrUgDON2Wn\n" +
                "7fhUNsgX+aem3SG7qacmgh8JLapwwMuSn2M4RqTbjPe8A7KIYwKj2bShr98ZsuN/\n" +
                "WRYQxdlQ82kdMJA5LLe9tlo5eGG2PHC/mqBCpAUgR9DJn3k0s5N1ztmTdMPkcGRK\n" +
                "9dedT/DiDl5kyYCbJE7/sfU4W7rFgCN9cx1QOCGCVny6NxFPqO/1Jg1PhecCAwEA\n" +
                "AQKCAgAYtPbbZohwoLr0FAeoo2QxJoPbMcWpION6ymD1OubePF8X9KKMgh/1oJSP\n" +
                "WZYH9I7/8AddXFCnzJzpTNyF+eLa9I4CNHP9LP1hObylWUuamd6PeBF2fD483AgR\n" +
                "9mvyUHw8sYt3lfCJwFPYTjWTFpQmgbQ/9Ed+cJjfxckvUg6pspm9Awto8zriAUbd\n" +
                "LfrbjzhTo9I/IpIJXDw5pmp2LJW+gB0M3oMsQRKAecsN+3LSRGjQLvhROfQfWJud\n" +
                "XPVvUeutZVI2y9qBPKfSrNPV3tjgcqn6VCSD81RmuBxjgqYTHc/XWn/uV9QVhlk2\n" +
                "zOqn3ZHZReAiJrq4r4uyzoxl7M/epvbAkNsPMs64lG+u4w+ezElh0/39jBZtFnLf\n" +
                "6yF+jL2AkaRtWfeteoE65sfudDrfSC7D7gmBenXPcx/wNy3CN+KNZw9aoZ8dFIw6\n" +
                "l1CJ8XPQLvuyPplLAbqzg3vTVDwHBcP82EfwWZ6JUwkdATwiTKlmEgXUFHQt6Pc9\n" +
                "BgVm6RKBOJcgKXxuix8VrxxuCml6FGFw6MyX+knwgsOvuExWhQ5Jt6/fHG6ctAb9\n" +
                "98BVpfQVFe1JGxWe/m6PJxykVZgU2yhhlpyD1rf9MgVLoJqr1DAMbEE7FnBxxqP8\n" +
                "sme5bxkw9ggSi+k391AywArhTRHkVwapLQeT8UEHOVJ6kzeXwQKCAQEAuk6337cy\n" +
                "gCwle+qMeMf8D8Wv97mZk3vZAlwTXQOK3Kvfx1B5gd6OSKQRKmPATHk5E89YrGiL\n" +
                "Q6LjHB/kYZxnsx4zsVTiPpq59egXK+GhEBJz2huG8TEC5w9UCq1RSOigSzwc60vN\n" +
                "uHrA6MnIdiElXA/qCRYJIm1RRmCDatZU9v4AIc+uPH+BFhsUcu+f9mMYY8ONhLh8\n" +
                "+0cdWh5bud1yrRhDsIBqsi6OetC6a5lFO393VCi0J4kBDdewcsphiIJt0n45jEi6\n" +
                "fgy+/Mv51IIV2M9wwiE3y8sr9z6EdTxaj5XFcpu/zcQHLr0hvthawKv9zUTtqRpA\n" +
                "ZLKj9s9CH/2GxwKCAQEA6U9L0hMUyGInm8YYIHrs36IKcntmiTOIJeVjC9oyWRfL\n" +
                "X0gOH74gMW8FYBL4I4IJsitmvQCb1Q38kgPv+J+TLHh165/TKsTjmJ6ikwl6w1R1\n" +
                "JOx8cjWdSG20yuERWhuvotuKbf4fyQN0GL3vJ2gHfKweHNEDEGYF8vhvbhAsb1Fj\n" +
                "v7ndSn3KQ4YyHfgSz63N4G4N3gxImbE1/3UKjDo8PsVcrVH8NQCZ0X5slXByNXn6\n" +
                "aliNrpyd6TWdh8sFJqEogCUsDCmjGRDRTi2xmvqBX24kcoFPPXgiKAhnMKbnLwP0\n" +
                "g501QeKFqJUD7dKB1gcvLogxgg8zJqQltJXvFa5n4QKCAQBSM7FFcBtEGjZfullI\n" +
                "YVSB0QOXFbkBLb/OHrocbBe0zdDqrEMwXfYBbwJz5ZdPJp1cdDv3thjcOt5h5mBq\n" +
                "AQxwJ38/WuiIHtS1/zVaEy9xW/Lp5QNhH/Lb6BN4sogO1zUCfP91gfi+0MqU65Mb\n" +
                "K/zD5fGpGKPnILyfhK33UaIjaGa+hFeoMcnO6xvWqou1tntoaTvjt1LNu87jmEl4\n" +
                "dJDIdtBG7CInhAsxfS8DkpjNa6xG4b67QuPIK8aan1jxVjsGiNLzZpOCUjVibM1K\n" +
                "SitBgiWKQgXQUVCQwnDHGV4LxFVeAAJc04UnxiNBhUxlfn15OFR0LDUFORzHJSNT\n" +
                "4hS5AoIBAQC8AlaLNy0sPUzFzuBqbCSuIhphHugF8CzqvOsQBglEmlQcuCgJlGDE\n" +
                "9T5TqXWkx4mgtjDYZcEt81NxJxMeOxmSYJBGDElS0c7Gki9YZe8zsL+lvZybPHE+\n" +
                "iImEY9Jj1qO2jUwETyC2D75iocwy6TX/VYP+nL/nWw+4OFMzUn08R04mllSpTqzL\n" +
                "tTx1wg8GpHyfOpCaOzPWBmQJM/wH/HXH76s7UDH5VD6f/0zpL/AY/+63/BxQdY0a\n" +
                "/UWww/2mn3/PpCTWuEbiUBca9N+GkA/pIwzj6Sg91K1RrVPadOUfkgRcezcLnMsw\n" +
                "isTbxSMzoh53VPDwaedCYEyyNZCw3BaBAoIBAQC4IdD3oLGA09ZIQT1gN5zJwC0q\n" +
                "5ZVHtKRnmwwEEs4z6FSmQJeSVrkfF7W6gPk84T1VAxfycTbR2jUu5+C2QuCMUODV\n" +
                "VccDCsTjn3pZfSOa8HB1ubDNv9ckhiDjWvbcHHOKn1Kf+bJv/3FQu9E7aqAjfG0+\n" +
                "3KbWQYhhW23NxQxPcP4MuUx1me5YuScg71hQ0SFwGB1tijEI9zV+UwWGBqwTFwRs\n" +
                "SckLpDz8uIeGvyJh8xWBJFZaCv8GvznJ1ZADWeKmRpcQKzryp39C/INfezxRZdqr\n" +
                "qgO1WP+yKuqsXdL65zATI1lN8x9mN0zSv10oxdvjEOMJrjbeaArztpbBbyVM\n" +
                "-----END RSA PRIVATE KEY-----";
        byte[] decrypt = decrypt(Hex.decode("6d987456ff522f266f7151ca9fe2ae593c0973b0c8e2100963fc328d4bcc5083713e8ef637f5864def2ef3a41642b79ed73b7383b5f76b543779907f0920c7444c7f55e9def4311e7faa61a65f0e6575bac3bcba6a6c067eeea54e9a03fa2f184817f3ef4032b6ac509aa0357bbb99804d291216ac2aa98121b5780adccafac8234dbdf8f2f21168badbbeed71fb0ab5daf91017ec324f033e5fbb9081494fe069e5947aaca35e4275c240674cdb35c9432db83b2fc84d8af8faeb768b8176d917e2625fe2f18c063fd17c6c06cd1bda7f8478ae5900c1ef4e71b12023ae3a66bda6eeb556a63f0fed6052cd54e201f099a3916964e59d87b21d78e6cfd4aa2f995b8785169c528c474bfc26b52c2144b1e0b94f3ca544a7c235f3bae9ab8b399fde6d7da1e2775329d23e6012cff5d1a8e9de65667019dc8b32c5afa949b5dff88c55c8bb887324c8fce22d5663115fd276dd61e5a848624f45714e1c7b1464e9598a1907e698e28d4f9d0b31a346b360544fcaf6afc55a1bd826d6dfd7663b0782c528f29fce291ff83e9a1a28c41e5108ef12e3e53e23c6f25fb9d511e64de3b9cba17988cd8d4a8700b7da4a40426b93a171dbd3e4c2348c254ca3087d8faa772ae75095d0cf7d635718aac1341e24b896a3751c5bc5f76ec869b0473702a2f406f1dadfe5b3ce5e1cf898d9ddf6ca944ccfa84ee29342bbd0ce942dfc85"), priv);
        //System.out.println(new String(decrypt));
        System.out.println("IV: " + Hex.toHexString(Arrays.copyOfRange(decrypt, 0, 16)));
        System.out.println("KEY: " + Hex.toHexString(Arrays.copyOfRange(decrypt, 16, 32)));
    }

    public void getSign() {
        // Get the instance of the Key Generator with "EC" algorithm
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec kpgparams = new ECGenParameterSpec("secp256r1");
            g.initialize(kpgparams);

            KeyPair pair = g.generateKeyPair();
            // Instance of signature class with SHA256withECDSA algorithm
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(pair.getPrivate());

            System.out.println("Private Keys is::" + pair.getPrivate());
            System.out.println("Public Keys is::" + pair.getPublic());

            ECPublicKey ecPublicKey = (ECPublicKey) pair.getPublic();
            System.out.println("x:" +ecPublicKey.getW().getAffineX().toString(16));
            System.out.println("y:" +ecPublicKey.getW().getAffineY().toString(16));

            System.out.println(Hex.toHexString(
                    Util.extractPublicKey(((ECPublicKey)pair.getPublic()).getW())
            ));

            String msg = "text ecdsa with sha256";//getSHA256(msg)
            ecdsaSign.update(msg.getBytes("UTF-8"));

            byte[] signature = ecdsaSign.sign();
            System.out.println("Signature is::"
                    + new BigInteger(1, signature).toString(16));

            // Validation
            ecdsaSign.initVerify(pair.getPublic());
            ecdsaSign.update(msg.getBytes("UTF-8"));
            if (ecdsaSign.verify(signature))
                System.out.println("valid");
            else
                System.out.println("invalid!!!!");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    @Test
    public void testSecp256R1() {
        String msg = "text ecdsa with sha256";
        String pub = "0459664f8561507ec61c9262bd37d792162bcd6cf27a0f375e155f5f821cb72c8b70ffba5e3147c539778af6471591dd255e05f42ed88a983f75904267e1e3d1f0";
        boolean b = Secp256r1.verify(Hex.decode(pub), msg.getBytes(), Hex.decode("30450220345bb5b6b37c5e23acf74331d4cc6f79385f9b5512e5cf695f3931908f5deacd0221009c7c1006edd74edaa7f8a6e25ee32ee2effcdab3d024b2dffc0ee5f5af06d96b"));
        assertTrue(b);
    }

    @Test
    public void testAES() {
        byte[] s = AES.decrypt(Hex.decode("e9ddc76100f77b5b07ed0764e4421f7fa5a703d2bb70d49e86d6939dd99069aa30d5dfb4ccdda1064c2cf74967aaa821aecea512880f8d3bf51bc136a97d130e1f87cd50dd1793e4bf32fa5d48944f37"),
                Hex.decode("998bf4aa619a86dffc75114a5e06964f"),
                Hex.decode("af1a5233cd63bf8cbaf4126c69eeaf85"));
        System.out.println(new String(s, StandardCharsets.US_ASCII));
    }

    public void WebAuthCallableUpgrade(byte[] bytes, String rsaPrivKey, byte[] r1pubkey) {
        try {
            byte[] encryptAesKeyIv = Arrays.copyOfRange(bytes, 0,512);
            byte[] encryptData = Arrays.copyOfRange(bytes, 512,bytes.length - 64);
            byte[] r1SigRS = Arrays.copyOfRange(bytes, bytes.length - 64, bytes.length);

            System.out.println("R1 sig RS: " + Hex.toHexString(r1SigRS));

            byte[] r1SigDER  = Der.toDer(r1SigRS);
            System.out.println("R1 sig DER: " + Hex.toHexString(r1SigDER));
            boolean verified = Secp256r1.verify(r1pubkey, Arrays.copyOfRange(bytes, 0,bytes.length - 64), r1SigDER);
            if (verified) {
                System.out.println("verified ok");
                byte[] keyAndIv = RSA.decrypt(encryptAesKeyIv, rsaPrivKey);
                byte[] aesIv = Arrays.copyOfRange(keyAndIv, 0,16);
                byte[] aesKey = Arrays.copyOfRange(keyAndIv, 16,32);

                System.out.println("aesIv: " + Hex.toHexString(aesIv));
                System.out.println("aesKey: " + Hex.toHexString(aesKey));

                System.out.println("encryptData:" + encryptData.length);

                byte[] dataForChip = AES.decrypt(encryptData, aesKey, aesIv);

            } else {
                System.out.println("verified fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testWebAuth() {
        String hex = "1ffdfa267ec2db60bb22fd151feb9e4bbf1bd7c154873475bc60735fcbcdaedffdeb526745bffe4944cf1295d36d9e9d3b111f1defb09b47323427d85a29a32609b04b8d467a13512d4ee5b9f523a6e678836e80cf4494acfc137311fb68a6f09871a856917b5868733d657893abded33deee1453898e8980e627c0af72aa2bc2a2c2a2d2138e8b7f24ed9e1e41c10d59018177d57017e391a79c12f058484a67241318d59041408efca31efad26b5268ba0a868b80661ab3d0c904a87d16aa060dd60dd59d87b4619a0eec2823a34820c49a46e3e47c45e4e9b088d0d33e22bbc9fe93cbb5682319c6f8cdac303801849b39d78eb6467a09bf471954a72a92361fd54227ed2f8ec7866b7f4c357eb45e0efc98aa8277282840aed2ec89752ee98e66000b8bb5d6be942275925a089bbf48f8947aeba4686c21668c3e92aef4428856d25f2a4ffc7cd870bcde1cea4ed4e6c10d72199866ec26024eb24965b6de8cbeaff8c4b73bc7fe319bfcfc3880d0819b72523d9e44b471f9db396b1c2c0292aadf79271da7cf665fb3af1a4e75c59938032948d55b135711d9b95e9cc1c31767efcd3ff7b0e87afecbc29c504989836180184191d28b541dbb98f3684b5936527dddba117636b2dd1d92eda15b8281a13838263366095db8ec50bb86d21bf3c09e3fe879e7ff8034459a7b1de543e5fc8ae3087dacfbfdde97db2a5c616ff5b8d2307cf203f66dbbb0d99e038607476afa7da3b953cba5885837ab501aafb08a38d9b8e7444e5ca26eface4190e07d5e98fc9a91a2d00d55e5d42ca053d9f8b702ee945028269adfe5bc0437f986a333de71b0e80eea1caedb2fb30fcc53952fb4e0a5ae44bccee919d7a7f98a042b0d160375ea590fd8c8cf3f2fcd0f37497453d7f4f2c94f0c1623512ef6b070083d4697738b71f5c199c96ba4254bc123508af7dc286cb84536320b2ca2afd2c8a982c1f91feb44ad9dded890a9d5e1758467fdf65e76f15cb6336de9204c6c2fa4b50eb491c5d59b7a1d0d38c8d3eeeab34d599f44874fe0c06187a7ccec8f0fcaf62874a3b10d8500e842e56c8c4aa91d7810a789cad3764f93a5e5038cbf1def41fcac1fec3ebdea15adcfe963055a5283fb7b87b58c9b422f4189b038252016463b14924d3cf787fbbbdd0fd3b6f09a1d6907411407b2bcfe022296cf5049d903654a0b6c7f1ab9ff35f49ae6af31e612833d789a598fdec73654023dd350871732fefff82736b445cbad59a58c6362aaf0908952236a58aa836c22dbffc8e584c944b8564ae6c4fc1373f8dd88d716c8bf2e979f53a5ebeac1b81e35add7f8ab08e051ebf32a9e7b087fc821607bb6a825ccfc509d6d90c4e6377fd4029482ad0157fe5a3b4f04e984b04d4eb2779227d0db79686aa11933ce8597d8230bf4ce3cd03e456d9c4b8d1de4c571971b877e02f5f8f7902bf13bc88feeb6096fac941c636ca6bc3b1018332bd0e810116cd8fd13d89bb2de7409012ca2502d7313a6d6a2989985798172afbbae1ce4357145c591714afa9198fd9ff2022d8f6977638ae84d9f853dc0d34ace7f221404919ca4bd26fa562f72c3a0ce425772ce65ee9aeeeabd9b8d29c89fa4cda9f619ea695b5a511c32e107628b27c0959";
        byte[] r1PubKey = Hex.decode("0419733d904a2cd833194784d31bcdb506a0bb5cc4bc3794ed8b6594435ddb470ccd35fddb811ff8c0c535db9645d364ff239d11ed5eef3197b81fcf5f68ae71df");
        String rsaPrivKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIJKQIBAAKCAgEAqcta004a1w61ZwL5Au+1rDEiipSLtMKWMkkHWpjbOfkcGOeT\n" +
                "b1fSUlIBssNFBfdmSF1479b/55Zk5d++7ttzbY4mkcLe8bfSEMf0Ke4KJP7po3TT\n" +
                "iH9VuhFd25yeVPdTeVK/pdbuUaYmHlqWtIaYxoWHJ2U3b4rLp2BjpDTiNGq4KYFJ\n" +
                "VKP/rNt4RH6ZwQrVUCvTTWpKrhqnlFWMUK/93RkbCoU0W9nrMTJEe9bS64zv1Byu\n" +
                "/zX8L+kZtR5h/Zo7TvWMBCLKNKWSHZmuhKq8BO1WIq/EFcsybLLw0pRsEgleHzh4\n" +
                "kr6b3GE3LcYSizkAY7YyakGvgP72n22oOwTszE2eIIh3UiJRYg2zr2IlGXxc0sgx\n" +
                "xEb92e2d7zqUP2UsaC+iKG7E2mrmRK/s7tRBVI4Sez9tquKWtOo1/p49fCgnDM2F\n" +
                "Nt1zu2YiH9Lr0pD9mPnEOj+2PIwql432JyaJ4WVazwYFCMuHEr/lzNBrUgDON2Wn\n" +
                "7fhUNsgX+aem3SG7qacmgh8JLapwwMuSn2M4RqTbjPe8A7KIYwKj2bShr98ZsuN/\n" +
                "WRYQxdlQ82kdMJA5LLe9tlo5eGG2PHC/mqBCpAUgR9DJn3k0s5N1ztmTdMPkcGRK\n" +
                "9dedT/DiDl5kyYCbJE7/sfU4W7rFgCN9cx1QOCGCVny6NxFPqO/1Jg1PhecCAwEA\n" +
                "AQKCAgAYtPbbZohwoLr0FAeoo2QxJoPbMcWpION6ymD1OubePF8X9KKMgh/1oJSP\n" +
                "WZYH9I7/8AddXFCnzJzpTNyF+eLa9I4CNHP9LP1hObylWUuamd6PeBF2fD483AgR\n" +
                "9mvyUHw8sYt3lfCJwFPYTjWTFpQmgbQ/9Ed+cJjfxckvUg6pspm9Awto8zriAUbd\n" +
                "LfrbjzhTo9I/IpIJXDw5pmp2LJW+gB0M3oMsQRKAecsN+3LSRGjQLvhROfQfWJud\n" +
                "XPVvUeutZVI2y9qBPKfSrNPV3tjgcqn6VCSD81RmuBxjgqYTHc/XWn/uV9QVhlk2\n" +
                "zOqn3ZHZReAiJrq4r4uyzoxl7M/epvbAkNsPMs64lG+u4w+ezElh0/39jBZtFnLf\n" +
                "6yF+jL2AkaRtWfeteoE65sfudDrfSC7D7gmBenXPcx/wNy3CN+KNZw9aoZ8dFIw6\n" +
                "l1CJ8XPQLvuyPplLAbqzg3vTVDwHBcP82EfwWZ6JUwkdATwiTKlmEgXUFHQt6Pc9\n" +
                "BgVm6RKBOJcgKXxuix8VrxxuCml6FGFw6MyX+knwgsOvuExWhQ5Jt6/fHG6ctAb9\n" +
                "98BVpfQVFe1JGxWe/m6PJxykVZgU2yhhlpyD1rf9MgVLoJqr1DAMbEE7FnBxxqP8\n" +
                "sme5bxkw9ggSi+k391AywArhTRHkVwapLQeT8UEHOVJ6kzeXwQKCAQEAuk6337cy\n" +
                "gCwle+qMeMf8D8Wv97mZk3vZAlwTXQOK3Kvfx1B5gd6OSKQRKmPATHk5E89YrGiL\n" +
                "Q6LjHB/kYZxnsx4zsVTiPpq59egXK+GhEBJz2huG8TEC5w9UCq1RSOigSzwc60vN\n" +
                "uHrA6MnIdiElXA/qCRYJIm1RRmCDatZU9v4AIc+uPH+BFhsUcu+f9mMYY8ONhLh8\n" +
                "+0cdWh5bud1yrRhDsIBqsi6OetC6a5lFO393VCi0J4kBDdewcsphiIJt0n45jEi6\n" +
                "fgy+/Mv51IIV2M9wwiE3y8sr9z6EdTxaj5XFcpu/zcQHLr0hvthawKv9zUTtqRpA\n" +
                "ZLKj9s9CH/2GxwKCAQEA6U9L0hMUyGInm8YYIHrs36IKcntmiTOIJeVjC9oyWRfL\n" +
                "X0gOH74gMW8FYBL4I4IJsitmvQCb1Q38kgPv+J+TLHh165/TKsTjmJ6ikwl6w1R1\n" +
                "JOx8cjWdSG20yuERWhuvotuKbf4fyQN0GL3vJ2gHfKweHNEDEGYF8vhvbhAsb1Fj\n" +
                "v7ndSn3KQ4YyHfgSz63N4G4N3gxImbE1/3UKjDo8PsVcrVH8NQCZ0X5slXByNXn6\n" +
                "aliNrpyd6TWdh8sFJqEogCUsDCmjGRDRTi2xmvqBX24kcoFPPXgiKAhnMKbnLwP0\n" +
                "g501QeKFqJUD7dKB1gcvLogxgg8zJqQltJXvFa5n4QKCAQBSM7FFcBtEGjZfullI\n" +
                "YVSB0QOXFbkBLb/OHrocbBe0zdDqrEMwXfYBbwJz5ZdPJp1cdDv3thjcOt5h5mBq\n" +
                "AQxwJ38/WuiIHtS1/zVaEy9xW/Lp5QNhH/Lb6BN4sogO1zUCfP91gfi+0MqU65Mb\n" +
                "K/zD5fGpGKPnILyfhK33UaIjaGa+hFeoMcnO6xvWqou1tntoaTvjt1LNu87jmEl4\n" +
                "dJDIdtBG7CInhAsxfS8DkpjNa6xG4b67QuPIK8aan1jxVjsGiNLzZpOCUjVibM1K\n" +
                "SitBgiWKQgXQUVCQwnDHGV4LxFVeAAJc04UnxiNBhUxlfn15OFR0LDUFORzHJSNT\n" +
                "4hS5AoIBAQC8AlaLNy0sPUzFzuBqbCSuIhphHugF8CzqvOsQBglEmlQcuCgJlGDE\n" +
                "9T5TqXWkx4mgtjDYZcEt81NxJxMeOxmSYJBGDElS0c7Gki9YZe8zsL+lvZybPHE+\n" +
                "iImEY9Jj1qO2jUwETyC2D75iocwy6TX/VYP+nL/nWw+4OFMzUn08R04mllSpTqzL\n" +
                "tTx1wg8GpHyfOpCaOzPWBmQJM/wH/HXH76s7UDH5VD6f/0zpL/AY/+63/BxQdY0a\n" +
                "/UWww/2mn3/PpCTWuEbiUBca9N+GkA/pIwzj6Sg91K1RrVPadOUfkgRcezcLnMsw\n" +
                "isTbxSMzoh53VPDwaedCYEyyNZCw3BaBAoIBAQC4IdD3oLGA09ZIQT1gN5zJwC0q\n" +
                "5ZVHtKRnmwwEEs4z6FSmQJeSVrkfF7W6gPk84T1VAxfycTbR2jUu5+C2QuCMUODV\n" +
                "VccDCsTjn3pZfSOa8HB1ubDNv9ckhiDjWvbcHHOKn1Kf+bJv/3FQu9E7aqAjfG0+\n" +
                "3KbWQYhhW23NxQxPcP4MuUx1me5YuScg71hQ0SFwGB1tijEI9zV+UwWGBqwTFwRs\n" +
                "SckLpDz8uIeGvyJh8xWBJFZaCv8GvznJ1ZADWeKmRpcQKzryp39C/INfezxRZdqr\n" +
                "qgO1WP+yKuqsXdL65zATI1lN8x9mN0zSv10oxdvjEOMJrjbeaArztpbBbyVM\n" +
                "-----END RSA PRIVATE KEY-----";

        System.out.println(rsaPrivKey);

        //System.out.println(Secp256r1.getPublicKey(r1PubKey));
        WebAuthCallableUpgrade(Hex.decode(hex), rsaPrivKey, r1PubKey);
    }

    @Test
    public void test11() {
        byte[] b = Base64.decode("BOg4fylDlzNxMFFTvtQBRsakfxaBJBPJf25sx8Iaim8v3h0ml9mnNCrUVJjBAeXyeGAX69NbAxbaAkNHT+6gJtU=");
        System.out.println(Hex.toHexString(b));
    }


}
