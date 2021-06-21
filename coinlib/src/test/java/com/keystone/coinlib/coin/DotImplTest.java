package com.keystone.coinlib.coin;


import com.keystone.coinlib.coins.polkadot.ChainProperty;
import com.keystone.coinlib.coins.polkadot.DOT.DotImpl;
import com.keystone.coinlib.coins.polkadot.TransactionEncoder;
import com.keystone.coinlib.coins.polkadot.TransactionEncoderBuilder;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DotImplTest {



    @Test
    public void testConstructTransaction() {
        DotImpl dot = new DotImpl("DOT");
        TransactionEncoder substrateTransactionInfo = new TransactionEncoderBuilder()
                .setChainProperty(ChainProperty.Polkadot)
                .setAmount(10000000000L)
                .setBlockNumber(1517092)
                .setValidityPeriod(4096)
                .setSpecVersion(19)
                .setTransactionVersion(5)
                .setTip(100000000L)
                .setNonce(19)
                .setDest("14BX2fAup13B79jAJhHDfrkNitWBXV6Fc6dYKjrsNmb8Fo7F")
                .setBlockHash("cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb")
                .createSubstrateTransactionInfo();

        try {
            byte[] result = substrateTransactionInfo.encode();
            assertArrayEquals(result, Hex.decode(
                    "0503" +
                            "8cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca963" +
                            "0700e40b5402" +
                            "4b62" +
                            "4c" +
                            "0284d717" +
                            "13000000" +
                            "05000000" +
                            "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3" +
                            "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb"));
        } catch (Exception e) {

        }
    }


}
