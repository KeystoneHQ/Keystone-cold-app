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

import com.keystone.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.keystone.coinlib.coins.BTC.Electrum.TxUtils;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ElectrumTxTest {
    @Test
    public void testParseTxSignalInput() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff0002000000000101bbd9dd3404ffd195aca4575bd850755a76dd33e0673442e948571d8b7e936ddb0000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff01a1eb00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff27ec00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b8000001004a830900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);
        assertEquals(2, tx.getVersion());
        assertTrue(tx.isPartial());
        assertTrue(tx.isSegwit());
        assertEquals(1, tx.getInputs().size());
        assertEquals("p2wpkh-p2sh", tx.getInputs().get(0).type);
        assertEquals(60455, tx.getInputs().get(0).value.intValue());
        assertEquals("02a9bcccbd7b9292aa4346ef973a7135473571a79f95ea2ef2466da2f43ba76f66", tx.getInputs().get(0).pubKey.pubkey);
        assertEquals(1, tx.getOutputs().size());
        assertEquals("3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ", tx.getOutputs().get(0).address);
    }


    @Test
    public void testParseTxMultiInputs() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff0002000000000106bf345375d61738afe374872e92de093cbbcbc56494822186591772de26b91104000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffffbf345375d61738afe374872e92de093cbbcbc56494822186591772de26b911040100000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff023c272923e8abc0d8edd491d6986cec5d5c28204490bd55654122fc3abf2914000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff44ba6e8c09775353eb6ef2b3b25aaeaaa6b48741671bb7fd0cdcd1b0aab1d731000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff595bd8050cd602c686849c636afe7684c044f67297b404c9fa390f5f275f35a1000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff5e730b6805a00247d5d140b7e89efaa983978963d42310fbb198ec961e7cd5e4000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff013ccb00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff1b0800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff788f00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100fefffffffff00700000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff220800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff220800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff552b00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b80000000056880900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);
        assertEquals(2, tx.getVersion());
        assertTrue(tx.isPartial());
        assertTrue(tx.isSegwit());
        assertEquals(6, tx.getInputs().size());
        assertEquals("p2wpkh-p2sh", tx.getInputs().get(0).type);
        assertEquals("02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67", tx.getInputs().get(0).pubKey.pubkey);
        assertEquals("02a9bcccbd7b9292aa4346ef973a7135473571a79f95ea2ef2466da2f43ba76f66", tx.getInputs().get(1).pubKey.pubkey);
        assertEquals("02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67", tx.getInputs().get(5).pubKey.pubkey);
        assertEquals(36728, tx.getInputs().get(1).value.intValue());
        assertEquals(1, tx.getOutputs().size());
        assertEquals("3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ", tx.getOutputs().get(0).address);
    }

    @Test
    public void testTxUtils() throws ElectrumTx.SerializationException {
        String rawHex = "ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100";
        TxUtils.PubKeyInfo pubKeyInfo = TxUtils.getPubKeyInfo(rawHex);
        assertEquals("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb", pubKeyInfo.xpub);
        assertEquals("M/49'/0'/0'/0/1", pubKeyInfo.hdPath);
    }

    @Test
    public void testTx() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff00020000000001065de964445cda78e446ced48cc775b989566e68ab3ec98f838b70c7be15ef35060000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff0450946ed5608cce509adf9a99bc480cf4a340326ab4ebc39e0f8650e64c580e010000001716001436def5f1b1a236723fd8892ec7a731666dcb6f7afdffffff4ea96eb5a19fe0187227cb1e8a6eb235bdb789e113ee427986e5aad9a38a7a200000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff8d726b19ff9a5d26bc3a2de5003f335538a7143f4b4e3d1d56d6bd6de13bfd780100000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff299c3c2890d4e9f304309e3510673809c60551a3fe30d74f51eb7d0d1747ffbc0000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff4e183bfc94644d331e5b1993a04d6effcf6d24113969ab841f497b76016b30f00000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff01204e00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff220200000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffffc50e00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b801000d00feffffffffe80300000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffffca0300000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffff580200000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffff0c3600000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b80000010098900900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);

        System.out.println(tx);
    }

    @Test
    public void testFinal() {
        String hex = "02000000000101069494a45bd029cd27da7d9de90172702e2bfda40b4ad67bf3cc700c4212e4ac01000000171600141ea4ea0a12d7c2b07e9084d36abd03a2edd72798fdffffff02b80b00000000000017a91472ec889523c81e94fd9d65701417d76b371ae3a5874f0e00000000000017a9141c174319445d98f4da84b51da9c8feb686b4fb198702473044022008ab229e2878e339cfb5c4a8589d5f8072024c74413df0d113f26ca0d680660402202e541f490b1937c15cd45323cefbcba4d07eb7cab089fcdb44f795c92e2c6826012102a18c6e271a995b162348b4332b63f13bf031617192d6232e809210ad5d85c382e5940900";
        assertFalse(ElectrumTx.isFinal(hex));

        String hex1 = "020000000001025573b0158c7ce0b25bb3d0cff7344e369ca3dbcee2ce4d888840fff49ae17cd00100000017160014e9cf9131d9c02a3a02d246bb4297b5606c6cb2f9ffffffffe014dc2363486a66e337b802da70722a32065f7a9e4fd4daf0e067aa31f8e58900000000171600143007abdafe8f875c3d3b714428e7761494a71f6cffffffff02f78f01000000000017a914915892366a6cdf24afa6e1c480db2ad88c6337808798895b000000000017a914915892366a6cdf24afa6e1c480db2ad88c633780870247304402206b891a2c6b2a95bb7b7e25275544f3dd761269392dde98ab65c8f8187194ce0502203061881a7d0e92fd19ac68b77a1fee35d7b6b7b72e8fce369a35088c1b35cca7012103fbe02e16d35d3c9c6772c75ba5d0d1387573724082266ea667c53b9d00decd72024730440220236f1c70df027dd7c0c862c9b89b28cb3fca6a96d4c73f7f565b7cc4b0fdff6902202262c87aec91d1ad9e661d807d5f9a8aaacd8a634028a74013cc2141c8750d13012102f325a85902d264dbcb0cbe144e9b2463f8252bd0c51bc19666f4c82461e4baa200000000";
        assertTrue(ElectrumTx.isFinal(hex1));
    }

    @Test
    public void testMasterPublicKeyMatched() throws ElectrumTx.SerializationException {
        String hex = "45505446ff00020000000001019c27c79ffc9ea5773030e2f31c7f4852fdec0a9d5d56bfd96bd5635faa9da1050000000017160014926abdcbe75e28d62ba442d0f2033eded710bd91fdffffff02b30b00000000000017a914f2c0b14d07c5ac95185487ba27d4256d8e83e76887102700000000000017a914460effb9083c27112687e637a29f0893e110615d87feffffffff4d3700000000000000000201ff53ff049d7cb203e0a197408000000043e91b2cfe457819db8a86101dbf60f9310cd3d95dafb7f4dc9fc3d816bb6c8a0276f4cca4bd18335cc482cf0c6dce442ecdb9eeda4ef8909e0c8beec6a35d416c00000900a09a0900";
        ElectrumTx electrumTx = ElectrumTx.parse(Hex.decode(hex));
        assertTrue(TxUtils.isMasterPublicKeyMatch("xpub6DJXnFtECztcYS2Gm5pyPpbbxuqMJ7BvqBtrTH1ZBjXraQJAgnHsgzBn9Q6HvXieoT7TNb1ynkyWmQDF64GCaybUYnXhof7McNZhfmFRgp3",electrumTx));
    }
}
