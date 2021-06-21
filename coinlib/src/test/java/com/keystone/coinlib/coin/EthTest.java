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

package com.keystone.coinlib.coin;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.ETH.AbiDecoder;
import com.keystone.coinlib.coins.ETH.Eth;
import com.keystone.coinlib.coins.ETH.EthImpl;
import com.keystone.coinlib.coins.ETH.StructuredDataEncoder;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class EthTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "0xE410157345be56688F43FF0D9e4B2B38Ea8F7828",
                "0xEEACb7a5e53600c144C0b9839A834bb4b39E540c",
                "0xA116800A72e56f91cF1677D40C9984f9C9f4B2c7",
                "0x4826BadaBC9894B3513e23Be408605611b236C0f",
                "0x8a1503beb17Ef02cC4Ff288b0A73583c4ce547c7",
                "0x996c7a5c9001da0401B61aD68EFd2424633F728E",
                "0xcAC3561d0b4Bec860ADCEBD70f8E6a8A5D45D752",
                "0x7173684414e3a9d5347e5d73b1E8718f3020A296",
                "0x4AAb5aD4FF70D7388182068a0B74BEEBA28B5068",
                "0x824C70B0000Abf51F6db46284dC217579f53f86a",
                "0x56FA9453B22867E0292301b68C2A737D8879441B",
                };
        String pubKey = "xpub6CNhtuXAHDs84AhZj5ALZB6ii4sP5LnDXaKDSjiy6kcBbiysq89cDrLG29poKvZtX9z4FchZKTjTyiPuDeiFMUd1H4g5zViQxt4tpkronJr";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Eth.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

    @Test
    public void testUniswap() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("uniswap.json");
        File f = new File(resource.getPath());
        AbiDecoder decoder = new AbiDecoder();
        decoder.addAbi(readString(f));
        AbiDecoder.DecodedMethod decodedMethod = decoder.decodeMethod("0x7ff36ab500000000000000000000000000000000000000000000000029a4e56756c9bcb80000000000000000000000000000000000000000000000000000000000000080000000000000000000000000e10aa6471b33845fae88dd7bbeb63c250da3a639000000000000000000000000000000000000000000000000000000005fc9d3820000000000000000000000000000000000000000000000000000000000000002000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc20000000000000000000000006b175474e89094c44da98b954eedeac495271d0f");
        assertTrue(decodedMethod != null);
        assertEquals(decodedMethod.name,"swapExactETHForTokens");
        assertEquals(decodedMethod.params.get(0).name,"amountOutMin");
        assertEquals(decodedMethod.params.get(0).value,new BigInteger("3000775483736046776"));
        assertEquals(decodedMethod.params.get(1).name,"path");
        assertArrayEquals((Object[]) decodedMethod.params.get(1).value,new String[]{
                "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
                "0x6b175474e89094c44da98b954eedeac495271d0f"});
        assertEquals(decodedMethod.params.get(2).name,"to");
        assertEquals(decodedMethod.params.get(2).value,"0xe10aa6471b33845fae88dd7bbeb63c250da3a639");
        assertEquals(decodedMethod.params.get(3).name,"deadline");
        assertEquals(decodedMethod.params.get(3).value,new BigInteger("1607062402"));
    }

    @Test
    public void testDeposit() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("eth2_deposit.json");
        File f = new File(resource.getPath());
        AbiDecoder decoder = new AbiDecoder();
        decoder.addAbi(readString(f));
        AbiDecoder.DecodedMethod decodedMethod = decoder.decodeMethod("0x22895118000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120f77dbe8be8a273697ddfc21215e06a8fdb99ab10fbadf53d5d3a19c68ab0d5f60000000000000000000000000000000000000000000000000000000000000030b3f71bac3bd307ce569b7d6bcbe4edddfdd5259f5c84c8552b600823ab461418198bce196772a88401c9376ffbbb1af600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000a175e6357abdb384081c5cfaf5b3479dd3b11b59c8166bf7b97f8fcc7c954700000000000000000000000000000000000000000000000000000000000000609704a10c46559feae0f9ac9cc4fc32304b1535aa56f20179b42ac41df56a86b201b73987529f6dd801e72ef5d967a9b3181081d38f423c05b352b1073c7eb36c800929c5be5d14c3bc2fd3578c5eda86d1dbb2af252fc7d677e4f0f05c2ea4f8");
        assertTrue(decodedMethod != null);
        assertEquals(decodedMethod.name,"deposit");
        assertEquals(decodedMethod.params.get(0).name,"pubkey");
        assertEquals(decodedMethod.params.get(0).value,"0xb3f71bac3bd307ce569b7d6bcbe4edddfdd5259f5c84c8552b600823ab461418198bce196772a88401c9376ffbbb1af6");
        assertEquals(decodedMethod.params.get(1).name,"withdrawal_credentials");
        assertEquals(decodedMethod.params.get(1).value,"0x00a175e6357abdb384081c5cfaf5b3479dd3b11b59c8166bf7b97f8fcc7c9547");
        assertEquals(decodedMethod.params.get(2).name,"signature");
        assertEquals(decodedMethod.params.get(2).value,"0x9704a10c46559feae0f9ac9cc4fc32304b1535aa56f20179b42ac41df56a86b201b73987529f6dd801e72ef5d967a9b3181081d38f423c05b352b1073c7eb36c800929c5be5d14c3bc2fd3578c5eda86d1dbb2af252fc7d677e4f0f05c2ea4f8");
        assertEquals(decodedMethod.params.get(3).name,"deposit_data_root");
        assertEquals(decodedMethod.params.get(3).value,"0xf77dbe8be8a273697ddfc21215e06a8fdb99ab10fbadf53d5d3a19c68ab0d5f6");
    }

    public static String readString(@NonNull File file) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Test
    public void test() {
        BigInteger nonce = new BigInteger("202");
        BigInteger gasPrice = new BigInteger("b2d05e00",16);
        BigInteger gasLimit = new BigInteger("21660",16);
        String to = "0x3535353535353535353535353535353535353535";
        BigInteger value = new BigInteger("1000000000000000000");
        String data = FunctionEncoder.encode(new EthImpl(Eth.CHAIN_ID).transfer("0xeeacb7a5e53600c144c0b9839a834bb4b39e540c",new BigInteger("1000000000000000000")));
        RawTransaction transaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,"0xea26c4ac16d4a5a106820bc8aee85fd0b7b2b664", BigInteger.ZERO,data);
        Credentials credentials = Credentials.create("1e799db5ff3e2df04775afd82bdb3b02302f4d2cdab904cda426032d35768aed");
        byte[] signed = TransactionEncoder.signMessage(transaction,1, credentials);
        assertEquals(Hex.toHexString(Hash.sha3(signed)),"de664318df3576d68aded7f70f30ab712d058b71916cc105fc33d5e53fcbed5f");
    }

    @Test
    public void generateTx() {
        BigInteger gasPrice = new BigInteger("b2d05e00",16);
        BigInteger gasLimit = new BigInteger("21660",16);
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                new BigInteger("25"),
                gasPrice, gasLimit, "0x7a250d5630b4cf539739df2c5dacb4c659f2488d","0x18cbafe50000000000000000000000000000000000000000000000000000000016b89f3100000000000000000000000000000000000000000000000005846047980e016800000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000e9b6d149d8e67bf7876ce962a8c37414c56bc39e000000000000000000000000000000000000000000000000000000005ff2eef80000000000000000000000000000000000000000000000000000000000000002000000000000000000000000dac17f958d2ee523a2206206994597c13d831ec7000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2" );
        System.out.println(Hex.toHexString(TransactionEncoder.encode(rawTransaction,1)));
    }

    @Test
    public void testEip712() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("order.json");
        File f = new File(resource.getPath());
        String s = readString(f);
        byte[] messageHash = new StructuredDataEncoder(s).hashStructuredData();
        assertEquals("ccb29124860915763e8cd9257da1260abc7df668fde282272587d84b594f37f6", Hex.toHexString(messageHash));
    }

    @Test
    public void testVerify() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("test.json");
        File f = new File(resource.getPath());
        String s = readString(f);
        StructuredDataEncoder structuredData = new StructuredDataEncoder(s);
        byte[] msgHash = structuredData.hashStructuredData();
        String ownerAddress = "0xE10AA6471B33845FaE88DD7bBeB63c250DA3a639";
        String sig = "4f4e0dec5f1d6f99460fea04bc374d96a495d96c0c455d4731902cdbf95525fc77c66ee08346466fe188f521c196f0f06bebb8d1a8df44cb68792514ca24808e2a";
        byte[] signatureBytes = Hex.decode(sig);
        byte v = signatureBytes[64];
        Sign.SignatureData sd =
                new Sign.SignatureData(
                        v,
                        Arrays.copyOfRange(signatureBytes, 0, 32),
                        Arrays.copyOfRange(signatureBytes, 32, 64));

        boolean verified = false;
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey =
                    Sign.recoverFromSignature(
                            (byte) i,
                            new ECDSASignature(
                                    new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
                            msgHash);

            if (publicKey != null) {
                String addressRecovered = Keys.toChecksumAddress(Keys.getAddress(publicKey));
                if (addressRecovered.equals(ownerAddress)) {
                    verified = true;
                    break;
                }
            }
        }
        assertTrue(verified);
    }

}










