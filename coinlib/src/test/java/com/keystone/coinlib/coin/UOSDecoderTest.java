package com.keystone.coinlib.coin;

import com.keystone.coinlib.coins.polkadot.UOS.Extrinsic;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.UOS.UosDecodeResult;
import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.coinlib.coins.polkadot.UOS.UOSDecoder;
import com.keystone.coinlib.coins.polkadot.pallets.balance.TransferParameter;
import com.keystone.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UOSDecoderTest {

    @Test
    public void testDecodeUOSRawData() throws InvalidUOSException {
        String UOSRawData = "400c0000001000053010228b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827fa005038cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca9630700e40b5402d500240284d717190000000500000091b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c30ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec";
        UosDecodeResult result = new UOSDecoder().decode(UOSRawData);
        SubstratePayload sp = result.getSubstratePayload();
        assertEquals(result.frameCount, 1);
        assertEquals(result.currentFrame, 0);
        assertFalse(result.isMultiPart);

        assertEquals(sp.curve, "sr25519");
        assertFalse(sp.isHash);
        assertEquals(sp.getAccount(), "1vQAnWwUYeEnoF1yK51ZmHpaVRs6inHHEJhzJto3xgqe4pF");
        Network network  = sp.network;
        assertEquals(network.SS58Prefix, (byte) 0);
        assertEquals(network.genesisHash, "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3");
        assertEquals(network.name, "Polkadot");

        assertArrayEquals(sp.getSigningPayload(), Hex.decode("0503" +
                "8cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca963" +
                "0700e40b5402" +
                "d500" +
                "24" +
                "0284d717" +
                "1900000005000000" +
                "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3" +
                "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb"));

        Extrinsic extrinsic = sp.extrinsic;

        assertEquals(extrinsic.palletParameter.name, "balance.transferKeepAlive");
        TransferParameter tp = (TransferParameter) extrinsic.palletParameter;

        assertEquals(tp.getDestination(), "14BX2fAup13B79jAJhHDfrkNitWBXV6Fc6dYKjrsNmb8Fo7F");

        assertEquals(extrinsic.getEra(), "d500");
        assertEquals(extrinsic.getNonce(), "9");
        assertEquals(extrinsic.getTip(), "0.01");
        assertEquals(extrinsic.getSpecVersion(), 25);
        assertEquals(extrinsic.getTransactionVersion(), 5);
        assertEquals(extrinsic.getGenesisHash(), "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3");
        assertEquals(extrinsic.getBlockHash(), "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb");
    }

    @Test
    public void testBatch() throws InvalidUOSException, JSONException {
        String hex = "4016b000001000053010228b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827f55031a020c070028b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827f0700e40b5402000900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007040284d717c5028c001a0000000500000091b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c30a7698e78f89881bbd43267273c91004604a133550ff9aa14a93f7a882a5634491b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c30ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec";
        UosDecodeResult result = new UOSDecoder().decode(hex);
        Map<String, String> m = toMap(result.getSubstratePayload().extrinsic.palletParameter.toJSON());
        System.out.println(result.getSubstratePayload().extrinsic.palletParameter.toJSON().toString(2));
    }
    public Map<String, String> toMap(JSONObject jsonObj) {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObj.get(key);
                map.put(key, value.toString());
            }
            return map;
        } catch (JSONException ignored) {

        }
        return null;
    }

}
